package com.epam.interfaces.web.client.interceptor;

import com.epam.infrastructure.logging.MdcConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

// Include JWT token and transaction id for the feign client
@Component
@Slf4j
public class FeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            log.warn("No request attributes available - cannot propagate headers");
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        propagateAuthorizationHeader(request, template);

        propagateTransactionIdHeader(request, template);
    }

    /**
     * Extract JWT token from incoming request and add to outgoing Feign request.
     */
    private void propagateAuthorizationHeader(HttpServletRequest request, RequestTemplate template) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && !authHeader.isEmpty()) {
            template.header("Authorization", authHeader);
            log.debug("Propagated Authorization header to downstream service");
        }
        else {
            log.warn("No Authorization header found - downstream call may fail authentication");
        }
    }

    /**
     * Get transaction ID from request attribute and add to outgoing Feign request.
     */
    private void propagateTransactionIdHeader(HttpServletRequest request, RequestTemplate template) {
        // Get transaction ID from request attribute (set by TransactionIdFilter)
        String transactionId = (String) request.getAttribute(MdcConstants.TRANSACTION_ID_HEADER);

        if (transactionId != null && !transactionId.isEmpty()) {
            template.header(MdcConstants.TRANSACTION_ID_HEADER, transactionId);
            log.debug("Propagated Transaction ID to downstream service: {}", transactionId);
        }
        else {
            log.warn("No Transaction ID found in request attributes - downstream service will generate new one");
        }
    }
}
