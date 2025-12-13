package com.epam.infrastructure.logging;

import java.io.IOException;
import java.util.UUID;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Filter to generate and manage transaction IDs for request tracking. Transaction ID is stored in MDC (Mapped
 * Diagnostic Context) and included in all logs.
 */
@Component
@Order(1)
@Slf4j
class TransactionIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Check if transaction ID exists in request header (from upstream services)
            String transactionId = httpRequest.getHeader(MdcConstants.TRANSACTION_ID_HEADER);

            // If not present, generate new transaction ID
            if (transactionId == null || transactionId.isEmpty()) {
                transactionId = generateTransactionId();
            }

            // Store in MDC
            MDC.put(MdcConstants.TRANSACTION_ID_MDC_KEY, transactionId);

            // Add to response header so downstream services can use it
            httpResponse.setHeader(MdcConstants.TRANSACTION_ID_HEADER, transactionId);

            log
                    .info(
                        "Transaction started - Method: {}, URI: {}, TransactionId: {}",
                        httpRequest.getMethod(),
                        httpRequest.getRequestURI(),
                        transactionId);

            // Continue with request processing
            chain.doFilter(request, response);

            log.info("Transaction completed - Status: {}, TransactionId: {}", httpResponse.getStatus(), transactionId);

        }
        finally {
            MDC.clear();
        }
    }

    /**
     * Generate unique transaction ID Format: TXN-{timestamp}-{uuid}
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}

}
