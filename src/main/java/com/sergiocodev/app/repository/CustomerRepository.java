package com.sergiocodev.app.repository;

import com.sergiocodev.app.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la gestión de clientes de la botica.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    /**
     * Busca un cliente por su número de documento (DNI/RUC).
     * @param documentNumber Número de identidad.
     * @return Optional con el cliente.
     */
    Optional<Customer> findByDocumentNumber(String documentNumber);

    /**
     * Comprueba si un número de documento ya está registrado.
     */
    boolean existsByDocumentNumber(String documentNumber);

    /**
     * Cuenta los clientes creados entre dos fechas.
     */
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.createdAt >= :start AND c.createdAt <= :end")
    long countByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Cuenta los clientes que tuvieron al menos una venta en el período.
     */
    @Query("SELECT COUNT(DISTINCT s.customer.id) FROM Sale s WHERE s.customer IS NOT NULL AND s.isVoided = false AND s.date >= :start AND s.date <= :end")
    long countActiveCustomersWithSales(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Top clientes por monto total de ventas.
     * Devuelve: [customerId, customerName, totalAmount, salesCount]
     */
    @Query("SELECT s.customer.id, s.customer.name, SUM(s.total), COUNT(s) " +
           "FROM Sale s WHERE s.customer IS NOT NULL AND s.isVoided = false AND s.date >= :start AND s.date <= :end " +
           "GROUP BY s.customer.id, s.customer.name ORDER BY SUM(s.total) DESC")
    List<Object[]> findTopCustomersByAmount(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Actividad por mes: nuevos clientes creados y activos con compras.
     * Devuelve: [yearMonth, newCustomers]
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m'), COUNT(c) " +
           "FROM Customer c WHERE c.createdAt >= :start AND c.createdAt <= :end " +
           "GROUP BY FUNCTION('DATE_FORMAT', c.createdAt, '%Y-%m') ORDER BY 1 ASC")
    List<Object[]> findNewCustomersByMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Clientes activos con ventas agrupados por mes.
     * Devuelve: [yearMonth, activeCustomers]
     */
    @Query("SELECT FUNCTION('DATE_FORMAT', s.date, '%Y-%m'), COUNT(DISTINCT s.customer.id) " +
           "FROM Sale s WHERE s.customer IS NOT NULL AND s.isVoided = false AND s.date >= :start AND s.date <= :end " +
           "GROUP BY FUNCTION('DATE_FORMAT', s.date, '%Y-%m') ORDER BY 1 ASC")
    List<Object[]> findActiveCustomersByMonth(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Clientes con ventas y sus totales para calcular ticket promedio.
     */
    @Query("SELECT s.customer.id, SUM(s.total), COUNT(s) " +
           "FROM Sale s WHERE s.customer IS NOT NULL AND s.isVoided = false AND s.date >= :start AND s.date <= :end " +
           "GROUP BY s.customer.id")
    List<Object[]> findCustomerSalesSummary(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Ventas recientes por cliente con su última fecha de compra.
     * Devuelve: [customerId, customerName, maxDate, sumTotal, countSales]
     */
    @Query("SELECT s.customer.id, s.customer.name, s.customer.documentNumber, s.customer.phone, s.customer.email, " +
           "MAX(s.date), SUM(s.total), COUNT(s) " +
           "FROM Sale s WHERE s.customer IS NOT NULL AND s.isVoided = false " +
           "GROUP BY s.customer.id, s.customer.name, s.customer.documentNumber, s.customer.phone, s.customer.email " +
           "ORDER BY MAX(s.date) DESC")
    List<Object[]> findRecentCustomerPurchases(org.springframework.data.domain.Pageable pageable);
}
