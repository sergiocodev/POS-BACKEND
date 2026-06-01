package com.sergiocodev.app.service.impl;

import com.sergiocodev.app.service.interfaces.SaleService;
import com.sergiocodev.app.service.interfaces.SaleInventoryService;
import com.sergiocodev.app.service.interfaces.SalePaymentService;
import com.sergiocodev.app.service.interfaces.SaleSunatService;

import com.sergiocodev.app.dto.sale.BarcodeScanResponse;
import com.sergiocodev.app.dto.sale.CartCalculationRequest;
import com.sergiocodev.app.dto.sale.CartCalculationResponse;
import com.sergiocodev.app.dto.sale.CartItemCalculation;
import com.sergiocodev.app.dto.sale.CartItemRequest;
import com.sergiocodev.app.dto.sale.ProductForSaleResponse;
import com.sergiocodev.app.dto.sale.ProductSearchResponse;
import com.sergiocodev.app.dto.sale.SaleRequest;
import com.sergiocodev.app.dto.sale.SaleResponse;
import com.sergiocodev.app.dto.company.CompanyMinimalResponse;
import com.sergiocodev.app.mapper.SaleMapper;
import com.sergiocodev.app.model.Sale;
import com.sergiocodev.app.model.CashSession;
import com.sergiocodev.app.model.Product;
import com.sergiocodev.app.model.ProductLot;
import com.sergiocodev.app.model.SaleItem;
import com.sergiocodev.app.model.SalePayment;
import com.sergiocodev.app.repository.SaleRepository;
import com.sergiocodev.app.repository.CustomerRepository;
import com.sergiocodev.app.repository.EstablishmentRepository;
import com.sergiocodev.app.repository.UserRepository;
import com.sergiocodev.app.repository.ProductRepository;
import com.sergiocodev.app.repository.ProductLotRepository;
import com.sergiocodev.app.repository.CashSessionRepository;
import com.sergiocodev.app.repository.ProductUnitRepository;
import com.sergiocodev.app.model.ProductUnit;
import com.sergiocodev.app.model.Customer;
import com.sergiocodev.app.model.DocumentType;
import com.sergiocodev.app.model.Company;
import com.sergiocodev.app.model.DocumentSequence;
import com.sergiocodev.app.repository.CompanyRepository;
import com.sergiocodev.app.repository.DocumentSequenceRepository;
import com.sergiocodev.app.exception.BadRequestException;
import com.sergiocodev.app.exception.ResourceNotFoundException;
import com.sergiocodev.app.exception.StockInsufficientException;
import com.sergiocodev.app.util.PdfGenerator;
import com.sergiocodev.app.dto.sunat.EmitInvoiceResponse;
import com.sergiocodev.app.model.Sale.SunatStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleServiceImpl implements SaleService {

        private final SaleRepository repository;
        private final CustomerRepository customerRepository;
        private final EstablishmentRepository establishmentRepository;
        private final UserRepository userRepository;
        private final ProductRepository productRepository;
        private final ProductLotRepository lotRepository;
        private final CashSessionRepository cashSessionRepository;
        private final ProductUnitRepository productUnitRepository;
        private final DocumentSequenceRepository documentSequenceRepository;
        private final CompanyRepository companyRepository;
        private final SaleMapper mapper;
        private final SaleInventoryService saleInventoryService;
        private final SalePaymentService salePaymentService;
        private final SaleSunatService saleSunatService;

        @Override
        @Transactional
        public SaleResponse create(SaleRequest request, Long userId) {
                log.info("Creating sale: establishment={}, customer={}, items={}, userId={}",
                                request.establishmentId(), request.customerId(), request.items().size(), userId);

                Sale entity = mapper.toEntity(request);
                entity.setEstablishment(establishmentRepository.findById(request.establishmentId())
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Establishment not found: "
                                                                + request.establishmentId())));
                if (request.customerId() != null) {
                        entity.setCustomer(customerRepository.findById(request.customerId()).orElse(null));
                } else {
                        // Assign default customer: "Público en General" with document "00000000"
                        Customer defaultCustomer = customerRepository.findByDocumentNumber("00000000")
                                        .orElseGet(() -> {
                                                Customer generic = new Customer();
                                                generic.setName("PUBLICO EN GENERAL");
                                                generic.setDocumentNumber("00000000");
                                                generic.setDocumentType(DocumentType.DNI);
                                                return customerRepository.save(generic);
                                        });
                        entity.setCustomer(defaultCustomer);
                }
                entity.setUser(userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId)));

                String series = request.series() != null ? request.series()
                                : (request.documentType() == Sale.SaleDocumentType.FACTURA ? "F001" : "B001");
                entity.setSeries(series);
                entity.setNumber(generateNextNumber(request.establishmentId(), request.documentType(), series));

                entity.setDate(LocalDateTime.now());
                entity.setStatus(Sale.SaleStatus.COMPLETED);

                // Determine payment condition
                entity.setPaymentCondition(request.paymentCondition() != null ? request.paymentCondition()
                                : Sale.PaymentCondition.CONTADO);

                CashSession session = cashSessionRepository
                                .findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                                .orElseThrow(() -> new BadRequestException(
                                                "No specific active cash session found for user. Please open a cash session before making a sale."));
                entity.setCashSession(session);

                for (var ir : request.items()) {
                        Product product = productRepository.findById(ir.productId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Product not found: " + ir.productId()));
                        ProductLot lot = ir.lotId() != null ? lotRepository.findById(ir.lotId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Lot not found: " + ir.lotId()))
                                        : null;

                        ProductUnit productUnit = productUnitRepository.findById(ir.productUnitId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "ProductUnit not found: " + ir.productUnitId()));

                        // Calcular la cantidad en unidad base (ej: 2 blísteres × factor 10 = 20
                        // tabletas)
                        int factor = productUnit.getFactor() != null ? productUnit.getFactor() : 1;
                        BigDecimal baseQuantity = ir.quantity().multiply(new BigDecimal(factor));

                        if (lot != null) {
                                saleInventoryService.validateStock(request.establishmentId(), lot.getId(), baseQuantity);
                        }

                        SaleItem item = mapper.toItemEntity(ir);
                        item.setSale(entity);
                        item.setProduct(product);
                        item.setProductUnit(productUnit);
                        item.setLot(lot);

                        BigDecimal grossAmount = ir.unitPrice().multiply(ir.quantity());
                        BigDecimal discount = ir.discountAmount() != null ? ir.discountAmount() : BigDecimal.ZERO;
                        BigDecimal increase = ir.increaseAmount() != null ? ir.increaseAmount() : BigDecimal.ZERO;
                        BigDecimal netAmount = grossAmount.subtract(discount).add(increase);

                        item.setAmount(netAmount);
                        item.setDiscountAmount(discount);
                        item.setDiscountReason(ir.discountReason());
                        item.setIncreaseAmount(increase);
                        item.setIncreaseReason(ir.increaseReason());
                        item.setAppliedTaxRate(product.getTaxType().getRate());
                        entity.getItems().add(item);

                        if (lot != null) {
                                saleInventoryService.updateInventory(entity, item, baseQuantity);
                        }
                }

                calculateTotals(entity);

                salePaymentService.processPayments(request, entity, session);

                log.debug("Saving sale: items={}, payments={}, total={}",
                                entity.getItems().size(), entity.getPayments().size(), entity.getTotal());
                Sale savedSale = repository.save(entity);
                log.info("Sale created successfully: id={}, series={}, number={}, total={}",
                                savedSale.getId(), savedSale.getSeries(), savedSale.getNumber(), savedSale.getTotal());

                salePaymentService.createAccountReceivableIfCredit(savedSale, request);

                return mapper.toResponse(savedSale);
        }

        private SaleResponse addCompanyInfo(SaleResponse response) {
                Company company = companyRepository.findMainCompany().orElse(null);
                if (company != null) {
                        com.sergiocodev.app.dto.company.CompanyMinimalResponse companyInfo = new com.sergiocodev.app.dto.company.CompanyMinimalResponse(
                                        company.getRuc(),
                                        company.getName(),
                                        company.getAddress(),
                                        company.getUbigeo(),
                                        company.getUrbanization(),
                                        company.getPhone(),
                                        company.getEmail(),
                                        company.getLogoUrl());
                        return new SaleResponse(
                                        response.id(),
                                        response.establishmentName(),
                                        response.customerName(),
                                        response.username(),
                                        response.documentType(),
                                        response.series(),
                                        response.number(),
                                        response.date(),
                                        response.subTotal(),
                                        response.tax(),
                                        response.total(),
                                        response.status(),
                                        response.paymentCondition(),
                                        response.sunatStatus(),
                                        response.pdfUrl(),
                                        response.cdrUrl(),
                                        response.sunatResponseJson(),
                                        response.sunatErrorCode(),
                                        response.relatedSaleId(),
                                        response.noteCode(),
                                        response.noteReason(),
                                        response.voided(),
                                        response.voidedAt(),
                                        response.voidReason(),
                                        response.items(),
                                        response.payments(),
                                        companyInfo,
                                        response.customerDocumentType(),
                                        response.customerDocumentNumber(),
                                        response.customerAddress(),
                                        response.userFullName());
                }
                return response;
        }


        private Sale calculateTotals(Sale sale) {
                BigDecimal total = BigDecimal.ZERO;
                BigDecimal subTotal = BigDecimal.ZERO;

                for (SaleItem item : sale.getItems()) {
                        BigDecimal itemAmount = item.getAmount();
                        BigDecimal rate = item.getAppliedTaxRate() != null ? item.getAppliedTaxRate() : BigDecimal.ZERO;

                        BigDecimal divisor = BigDecimal.ONE.add(rate);
                        // Usamos 4 decimales para cálculos intermedios y evitar pérdida de precisión
                        BigDecimal itemSubTotal = itemAmount.divide(divisor, 4, java.math.RoundingMode.HALF_UP);

                        total = total.add(itemAmount);
                        subTotal = subTotal.add(itemSubTotal);
                }

                sale.setTotal(total.setScale(2, java.math.RoundingMode.HALF_UP));
                sale.setSubTotal(subTotal.setScale(2, java.math.RoundingMode.HALF_UP));
                sale.setTax(sale.getTotal().subtract(sale.getSubTotal()));
                return sale;
        }

        @Override
        @Transactional(readOnly = true)
        public List<SaleResponse> getAll(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
                List<Sale> sales;
                if (startDate != null && endDate != null) {
                        sales = repository.findByDateBetweenOrderByDateDesc(startDate, endDate);
                } else {
                        sales = repository.findAllByOrderByDateDesc();
                }

                return sales.stream()
                                .map(mapper::toResponse)
                                .map(this::addCompanyInfo)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public Page<SaleResponse> getAllPaged(
                        java.time.LocalDateTime startDate,
                        java.time.LocalDateTime endDate,
                        String documentType,
                        String series,
                        String number,
                        String customerName,
                        String customerDocument,
                        String vendedorName,
                        String status,
                        String sunatStatus,
                        String total,
                        String paymentMethod,
                        String columnDate,
                        Pageable pageable) {

                org.springframework.data.jpa.domain.Specification<Sale> spec = com.sergiocodev.app.specification.SaleSpecification
                                .filterSales(
                                                startDate, endDate, documentType, series, number, customerName,
                                                customerDocument, vendedorName, status, sunatStatus,
                                                total, paymentMethod, columnDate);

                Page<Sale> sales = repository.findAll(spec, pageable);
                return sales.map(mapper::toResponse).map(this::addCompanyInfo);
        }

        @Override
        @Transactional(readOnly = true)
        public SaleResponse getById(Long id) {
                return repository.findById(id)
                                .map(mapper::toResponse)
                                .map(this::addCompanyInfo)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Sale not found: " + id));
        }

        @Override
        @Transactional
        public void cancel(Long id) {
                Sale sale = repository.findById(id)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Sale not found: " + id));
                sale.setStatus(Sale.SaleStatus.CANCELED);
                repository.save(sale);
        }

        @Override
        @Transactional(readOnly = true)
        public byte[] getPdf(Long id) {
                return getSaleDocumentPDF(id, "A4");
        }

        @Override
        @Transactional(readOnly = true)
        public String getXml(Long id) {
                return saleSunatService.getXml(id);
        }

        @Override
        public String getCdr(Long id) {
                return saleSunatService.getCdr(id);
        }

        @Override
        @Transactional
        public SaleResponse createCreditNote(Long id, String reason, Long userId) {
                Sale original = repository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Original sale not found: " + id));

                if (original.getStatus() == Sale.SaleStatus.CANCELED || original.isVoided()) {
                        throw new IllegalStateException("Cannot create credit note for a canceled or voided sale");
                }

                Sale note = new Sale();
                note.setEstablishment(original.getEstablishment());
                note.setCustomer(original.getCustomer());
                note.setUser(userRepository.findById(userId).orElse(original.getUser()));
                note.setDocumentType(Sale.SaleDocumentType.NOTA_CREDITO);
                note.setRelatedSale(original);
                note.setNoteReason(reason);

                String series = "FC01"; // Default for NC
                note.setSeries(series);
                note.setNumber(
                                generateNextNumber(original.getEstablishment().getId(),
                                                Sale.SaleDocumentType.NOTA_CREDITO, series));

                note.setDate(LocalDateTime.now());
                note.setSubTotal(original.getSubTotal().negate());
                note.setTax(original.getTax().negate());
                note.setTotal(original.getTotal().negate());
                note.setStatus(Sale.SaleStatus.COMPLETED);

                for (SaleItem originalItem : original.getItems()) {
                        saleInventoryService.reverseInventory(original, originalItem, "Credit note - " + reason, userId);
                }

                CashSession currentSession = cashSessionRepository
                                .findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                                .orElse(null);

                salePaymentService.processRefund(original, note, userId, currentSession);

                return mapper.toResponse(repository.save(note));
        }

        @Override
        @Transactional
        public SaleResponse createDebitNote(Long id, String reason, Long userId) {
                throw new UnsupportedOperationException("Debit notes not yet implemented");
        }

        @Override
        @Transactional
        public void invalidate(Long id, String reason, Long userId) {
                Sale sale = repository.findById(id)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException("Sale not found: " + id));

                sale.setVoided(true);
                sale.setVoidedAt(LocalDateTime.now());
                sale.setVoidReason(reason);
                repository.save(sale);

                for (SaleItem item : sale.getItems()) {
                        saleInventoryService.reverseInventory(sale, item, "Void", userId);
                }

                CashSession currentSession = cashSessionRepository
                                .findByUserIdAndStatus(userId, CashSession.SessionStatus.OPEN)
                                .orElse(null);

                salePaymentService.processVoidRefund(sale, userId, currentSession);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ProductForSaleResponse> listProductsForSale(Long establishmentId) {
                return saleInventoryService.listProductsForSale(establishmentId);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ProductSearchResponse> searchProductsForPOS(String query, Long establishmentId) {
                return saleInventoryService.searchProductsForPOS(query, establishmentId);
        }

        @Override
        @Transactional(readOnly = true)
        public BarcodeScanResponse getProductByBarcode(String barcode, Long establishmentId) {
                return saleInventoryService.getProductByBarcode(barcode, establishmentId);
        }

        @Override
        @Transactional(readOnly = true)
        public CartCalculationResponse calculateCartTotals(
                        CartCalculationRequest request) {
                BigDecimal subTotal = BigDecimal.ZERO;
                BigDecimal totalTax = BigDecimal.ZERO;
                BigDecimal totalDiscount = BigDecimal.ZERO;
                BigDecimal total = BigDecimal.ZERO;
                java.util.Map<String, BigDecimal> taxBreakdown = new java.util.HashMap<>();
                List<CartItemCalculation> itemCalculations = new java.util.ArrayList<>();

                for (CartItemRequest itemReq : request.items()) {
                        Product product = productRepository.findById(itemReq.productId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Product not found: " + itemReq.productId()));

                        BigDecimal quantity = itemReq.quantity();
                        BigDecimal unitPrice = itemReq.unitPrice();
                        BigDecimal discount = itemReq.discountAmount() != null ? itemReq.discountAmount()
                                        : BigDecimal.ZERO;
                        BigDecimal increase = itemReq.increaseAmount() != null ? itemReq.increaseAmount()
                                        : BigDecimal.ZERO;

                        BigDecimal lineGross = unitPrice.multiply(quantity);
                        BigDecimal lineNet = lineGross.subtract(discount).add(increase);

                        // Tax
                        // Assuming price is INCLUSIVE of tax? Or exclusive?
                        // Usually POS prices are inclusive. If inclusive:
                        // Base = Total / (1 + Rate)
                        // Tax = Total - Base
                        // If exclusive:
                        // Tax = Total * Rate

                        // Let's assume INCLUSIVE for now as per previous code (0.18 hardcoded
                        // previously
                        // suggesting handling).
                        // Actually previous code: item.setAppliedTaxRate(new BigDecimal("0.18"));
                        // sale.setTax(subTotal.multiply(new BigDecimal("0.18")));
                        // This suggests exclusive in previous code? "subTotal" reduced, then tax added?
                        // "sale.setTotal(subTotal);" -> This suggests SubTotal was the Total.
                        // Let's look at previous create method:
                        // BigDecimal amount = ir.unitPrice().multiply(ir.quantity());
                        // item.setAmount(amount);
                        // ...
                        // BigDecimal subTotal = ... sum of amounts
                        // sale.setSubTotal(subTotal);
                        // sale.setTax(subTotal.multiply("0.18"));
                        // sale.setTotal(subTotal);
                        // This existing logic is weird. Total = SubTotal? Then Tax is extra but not
                        // added to Total?
                        // "sale.setTotal(subTotal);"
                        // Correct logic should be: Total = SubTotal + Tax (if exclusive) or Total =
                        // SubTotal (if inclusive).
                        // If SubTotal is inclusive, then Tax = SubTotal - (SubTotal / 1.18).

                        // I will implement standard logic:
                        // LineTotal = (Price * Qty) - Discount
                        // TaxAmount = LineTotal * Rate (if exclusive) or LineTotal - (LineTotal /
                        // (1+Rate)) (if inclusive)
                        // I'll assume INCLUSIVE because retail POS usually is.

                        BigDecimal infoTaxAmount;
                        if (product.getTaxType() != null
                                        && product.getTaxType().getRate().compareTo(BigDecimal.ZERO) > 0) {
                                // Inclusive
                                BigDecimal div = BigDecimal.ONE.add(product.getTaxType().getRate());
                                BigDecimal base = lineNet.divide(div, 2, java.math.RoundingMode.HALF_UP);
                                infoTaxAmount = lineNet.subtract(base);
                        } else {
                                infoTaxAmount = BigDecimal.ZERO;
                        }

                        subTotal = subTotal.add(lineNet);
                        totalTax = totalTax.add(infoTaxAmount);
                        totalDiscount = totalDiscount.add(discount);

                        String taxName = product.getTaxType() != null ? product.getTaxType().getName() : "IGV";
                        taxBreakdown.merge(taxName, infoTaxAmount, BigDecimal::add);

                        itemCalculations.add(new CartItemCalculation(
                                        product.getId(),
                                        quantity,
                                        unitPrice,
                                        discount,
                                        increase,
                                        infoTaxAmount,
                                        lineNet));
                }

                if (request.globalDiscount() != null) {
                        BigDecimal glDisc = request.globalDiscount();
                        totalDiscount = totalDiscount.add(glDisc);
                        subTotal = subTotal.subtract(glDisc);
                }

                total = subTotal;

                return new CartCalculationResponse(
                                subTotal.subtract(totalTax),
                                totalTax,
                                totalDiscount,
                                total,
                                taxBreakdown,
                                itemCalculations);
        }

        @Override
        @Transactional
        public SaleResponse processSaleTransaction(SaleRequest request, Long userId) {
                return create(request, userId);
        }

        @Override
        @Transactional(readOnly = true)
        public byte[] getSaleDocumentPDF(Long id, String format) {
                Sale sale = repository.findWithItemsById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Sale not found: " + id));
                SaleResponse saleResponse = mapper.toResponse(sale);
                saleResponse = addCompanyInfo(saleResponse);

                CompanyMinimalResponse companyInfo = saleResponse.company();
                return PdfGenerator.generateSalePdf(saleResponse, companyInfo, format);
        }

        @Override
        @Transactional
        public EmitInvoiceResponse emitInvoiceToOSE(Long saleId) {
                return saleSunatService.emitInvoiceToOSE(saleId);
        }

        private String generateNextNumber(Long establishmentId, Sale.SaleDocumentType saleDocType, String series) {
                DocumentSequence.DocumentType seqDocType = mapDocType(saleDocType);

                DocumentSequence sequence = documentSequenceRepository
                                .findForUpdate(establishmentId, seqDocType, series)
                                .orElseGet(() -> {
                                        DocumentSequence newSeq = new DocumentSequence();
                                        newSeq.setEstablishment(establishmentRepository.findById(establishmentId)
                                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                                        "Establishment not found: "
                                                                                        + establishmentId)));
                                        newSeq.setDocumentType(seqDocType);
                                        newSeq.setSeries(series);
                                        newSeq.setCurrentNumber(0);
                                        return newSeq;
                                });

                sequence.setCurrentNumber(sequence.getCurrentNumber() + 1);
                documentSequenceRepository.save(sequence);

                return String.format("%06d", sequence.getCurrentNumber());
        }

        private DocumentSequence.DocumentType mapDocType(Sale.SaleDocumentType saleDocType) {
                switch (saleDocType) {
                        case BOLETA:
                                return DocumentSequence.DocumentType.BOLETA;
                        case FACTURA:
                                return DocumentSequence.DocumentType.FACTURA;
                        case TICKET:
                                return DocumentSequence.DocumentType.TICKET;
                        case NOTA_DE_VENTA:
                                return DocumentSequence.DocumentType.NOTA_DE_VENTA;
                        case NOTA_CREDITO:
                                return DocumentSequence.DocumentType.NOTA_CREDITO;
                        case NOTA_DEBITO:
                                return DocumentSequence.DocumentType.NOTA_DEBITO;
                        default:
                                return DocumentSequence.DocumentType.TICKET;
                }
        }

        @Override
        @Transactional(readOnly = true)
        public com.sergiocodev.app.dto.sale.SaleSummaryResponse getSummary(
                        java.time.LocalDateTime startDate,
                        java.time.LocalDateTime endDate,
                        String documentType,
                        String series,
                        String number,
                        String customerName,
                        String customerDocument,
                        String vendedorName,
                        String status,
                        String sunatStatus,
                        String total,
                        String paymentMethod,
                        String columnDate) {

                return repository.getSaleSummary(
                        startDate, endDate, documentType, series, number,
                        customerName, customerDocument, vendedorName, status, sunatStatus,
                        total, paymentMethod, columnDate);
        }
}
