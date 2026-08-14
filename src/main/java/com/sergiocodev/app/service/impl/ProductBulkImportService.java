package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.dto.product.BulkImportResult;
import com.sergiocodev.app.dto.product.BulkImportRowError;
import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.model.*;
import com.sergiocodev.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de importación masiva de productos desde archivos Excel (.xlsx).
 * - Busca las entidades de catálogo (marca, categoría, etc.) por nombre.
 * - Si no existen, las crea automáticamente (excepto tax_types y pharmaceutical_forms
 *   que tienen campos adicionales obligatorios).
 * - Si el código del producto ya existe, actualiza; si no, crea uno nuevo.
 */
@Service
@RequiredArgsConstructor
public class ProductBulkImportService {

    private static final Logger log = LoggerFactory.getLogger(ProductBulkImportService.class);

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final LaboratoryRepository laboratoryRepository;
    private final PresentationRepository presentationRepository;
    private final PharmaceuticalFormRepository pharmaceuticalFormRepository;
    private final TaxTypeRepository taxTypeRepository;

    // --- Columnas del template ---
    private static final String[] HEADERS = {
            "code", "digemidCode", "tradeName", "genericName", "description",
            "brandName", "categoryName", "laboratoryName", "presentationDescription",
            "pharmaceuticalFormName", "requiresPrescription", "isGeneric"
    };

    private static final String[] HEADER_LABELS = {
            "Código *", "Código DIGEMID", "Nombre Comercial *", "Nombre Genérico", "Descripción",
            "Marca *", "Categoría *", "Laboratorio *", "Presentación *",
            "Forma Farmacéutica *", "Requiere Receta (true/false)", "Es Genérico (true/false)"
    };

    /**
     * Genera un archivo Excel (.xlsx) con la plantilla de importación.
     */
    public byte[] generateTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Productos");

            // Estilo de cabecera
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Fila de cabecera
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADER_LABELS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADER_LABELS[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            // Fila de ejemplo
            Row exampleRow = sheet.createRow(1);
            String[] exampleValues = {
                    "PRD-001", "DG12345", "Paracetamol 500mg", "Paracetamol",
                    "Analgésico y antipirético", "Genéricos Lab", "Analgésicos",
                    "Lab Peru", "Caja x 100", "Tableta", "false", "true"
            };
            for (int i = 0; i < exampleValues.length; i++) {
                exampleRow.createCell(i).setCellValue(exampleValues[i]);
            }

            // Hoja de instrucciones
            Sheet instructionsSheet = workbook.createSheet("Instrucciones");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            int row = 0;
            Row titleRow = instructionsSheet.createRow(row++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Instrucciones de Importación Masiva de Productos");
            titleCell.setCellStyle(titleStyle);
            row++;

            String[] instructions = {
                    "1. Complete los datos en la hoja 'Productos'.",
                    "2. Los campos marcados con * son obligatorios.",
                    "3. Las marcas, categorías, laboratorios, presentaciones y formas farmacéuticas se crean automáticamente si no existen.",
                    "4. El tipo de impuesto se asigna automáticamente (IGV por defecto).",
                    "5. Si un producto con el mismo código ya existe, será actualizado.",
                    "6. Los campos 'Requiere Receta' y 'Es Genérico' aceptan: true, false, si, no, 1, 0.",
                    "7. No modifique los encabezados de las columnas.",
                    "8. Puede agregar tantas filas como necesite.",
                    "9. Guarde el archivo como .xlsx antes de importar."
            };
            for (String instruction : instructions) {
                instructionsSheet.createRow(row++).createCell(0).setCellValue(instruction);
            }
            instructionsSheet.setColumnWidth(0, 20000);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar plantilla de importación: {}", e.getMessage(), e);
            throw new BadRequestException("No se pudo generar la plantilla de importación");
        }
    }

    /**
     * Procesa un archivo Excel subido y realiza la importación masiva.
     */
    @Transactional
    public BulkImportResult importFromExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo está vacío");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new BadRequestException("El archivo debe ser de tipo .xlsx (Excel)");
        }

        List<BulkImportRowError> errors = new ArrayList<>();
        int totalRows = 0;
        int created = 0;
        int updated = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            if (sheet == null || sheet.getPhysicalNumberOfRows() <= 1) {
                throw new BadRequestException("El archivo no contiene datos. Asegúrese de usar la plantilla correcta.");
            }

            // Iterar desde la fila 1 (saltando cabecera)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                totalRows++;
                int rowNum = i + 1; // Número de fila visible en Excel (1-based)

                try {
                    String code = getCellString(row, 0);
                    String digemidCode = getCellString(row, 1);
                    String tradeName = getCellString(row, 2);
                    String genericName = getCellString(row, 3);
                    String description = getCellString(row, 4);
                    String brandName = getCellString(row, 5);
                    String categoryName = getCellString(row, 6);
                    String laboratoryName = getCellString(row, 7);
                    String presentationDesc = getCellString(row, 8);
                    String pharmaFormName = getCellString(row, 9);
                    boolean requiresPrescription = getCellBoolean(row, 10);
                    boolean isGeneric = getCellBoolean(row, 11);

                    // Validaciones de campos obligatorios
                    List<String> missingFields = new ArrayList<>();
                    if (code == null || code.isBlank()) missingFields.add("Código");
                    if (tradeName == null || tradeName.isBlank()) missingFields.add("Nombre Comercial");
                    if (brandName == null || brandName.isBlank()) missingFields.add("Marca");
                    if (categoryName == null || categoryName.isBlank()) missingFields.add("Categoría");
                    if (laboratoryName == null || laboratoryName.isBlank()) missingFields.add("Laboratorio");
                    if (presentationDesc == null || presentationDesc.isBlank()) missingFields.add("Presentación");
                    if (pharmaFormName == null || pharmaFormName.isBlank()) missingFields.add("Forma Farmacéutica");

                    if (!missingFields.isEmpty()) {
                        errors.add(new BulkImportRowError(rowNum, code, tradeName,
                                "Campos obligatorios vacíos: " + String.join(", ", missingFields)));
                        continue;
                    }

                    // Resolver entidades de catálogo
                    Brand brand = resolveOrCreateBrand(brandName);
                    Category category = resolveOrCreateCategory(categoryName);
                    Laboratory laboratory = resolveOrCreateLaboratory(laboratoryName);
                    Presentation presentation = resolveOrCreatePresentation(presentationDesc);

                    // Forma farmacéutica: buscar o crear automáticamente
                    PharmaceuticalForm pharmaForm = resolveOrCreatePharmaceuticalForm(pharmaFormName);

                    // Tipo de impuesto por defecto (IGV, id=1)
                    TaxType taxType = taxTypeRepository.findById(1L)
                            .orElseThrow(() -> new BadRequestException("Tipo de impuesto por defecto no configurado (id=1)"));

                    // Verificar si el producto ya existe (por código)
                    Product existingProduct = productRepository.findByCode(code.trim()).orElse(null);

                    if (existingProduct != null) {
                        // ACTUALIZAR
                        existingProduct.setDigemidCode(digemidCode);
                        existingProduct.setTradeName(tradeName.trim());
                        existingProduct.setGenericName(genericName);
                        existingProduct.setDescription(description);
                        existingProduct.setBrand(brand);
                        existingProduct.setCategory(category);
                        existingProduct.setLaboratory(laboratory);
                        existingProduct.setPresentation(presentation);
                        existingProduct.setPharmaceuticalForm(pharmaForm);
                        existingProduct.setTaxType(taxType);
                        existingProduct.setRequiresPrescription(requiresPrescription);
                        existingProduct.setGeneric(isGeneric);
                        productRepository.save(existingProduct);
                        updated++;
                    } else {
                        // CREAR
                        Product newProduct = new Product();
                        newProduct.setCode(code.trim());
                        newProduct.setDigemidCode(digemidCode);
                        newProduct.setTradeName(tradeName.trim());
                        newProduct.setGenericName(genericName);
                        newProduct.setDescription(description);
                        newProduct.setBrand(brand);
                        newProduct.setCategory(category);
                        newProduct.setLaboratory(laboratory);
                        newProduct.setPresentation(presentation);
                        newProduct.setPharmaceuticalForm(pharmaForm);
                        newProduct.setTaxType(taxType);
                        newProduct.setRequiresPrescription(requiresPrescription);
                        newProduct.setGeneric(isGeneric);
                        productRepository.save(newProduct);
                        created++;
                    }

                } catch (Exception e) {
                    String code = getCellString(row, 0);
                    String tradeName = getCellString(row, 2);
                    log.warn("Error procesando fila {}: {}", rowNum, e.getMessage());
                    errors.add(new BulkImportRowError(rowNum, code, tradeName,
                            "Error: " + e.getMessage()));
                }
            }

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al leer archivo Excel: {}", e.getMessage(), e);
            throw new BadRequestException("Error al leer el archivo Excel: " + e.getMessage());
        }

        return new BulkImportResult(totalRows, created, updated, errors.size(), errors);
    }

    // ======== Métodos auxiliares ========

    private Brand resolveOrCreateBrand(String name) {
        return brandRepository.findByName(name.trim())
                .orElseGet(() -> {
                    Brand b = new Brand();
                    b.setName(name.trim());
                    return brandRepository.save(b);
                });
    }

    private Category resolveOrCreateCategory(String name) {
        return categoryRepository.findByName(name.trim())
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName(name.trim());
                    return categoryRepository.save(c);
                });
    }

    private Laboratory resolveOrCreateLaboratory(String name) {
        return laboratoryRepository.findByName(name.trim())
                .orElseGet(() -> {
                    Laboratory l = new Laboratory();
                    l.setName(name.trim());
                    return laboratoryRepository.save(l);
                });
    }

    private Presentation resolveOrCreatePresentation(String description) {
        return presentationRepository.findByDescription(description.trim())
                .orElseGet(() -> {
                    Presentation p = new Presentation();
                    p.setDescription(description.trim());
                    return presentationRepository.save(p);
                });
    }

    private PharmaceuticalForm resolveOrCreatePharmaceuticalForm(String name) {
        return pharmaceuticalFormRepository.findByName(name.trim())
                .orElseGet(() -> {
                    PharmaceuticalForm pf = new PharmaceuticalForm();
                    pf.setName(name.trim());
                    return pharmaceuticalFormRepository.save(pf);
                });
    }

    private String getCellString(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }

    private boolean getCellBoolean(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) return false;

        return switch (cell.getCellType()) {
            case BOOLEAN -> cell.getBooleanCellValue();
            case STRING -> {
                String val = cell.getStringCellValue().trim().toLowerCase();
                yield val.equals("true") || val.equals("si") || val.equals("sí") || val.equals("1") || val.equals("yes");
            }
            case NUMERIC -> cell.getNumericCellValue() == 1.0;
            default -> false;
        };
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellString(row, c);
                if (val != null && !val.isBlank()) return false;
            }
        }
        return true;
    }
}
