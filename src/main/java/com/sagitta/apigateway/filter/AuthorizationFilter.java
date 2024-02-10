package com.sagitta.apigateway.filter;

import com.sagitta.apigateway.domain.AccessToken;
import com.sagitta.apigateway.domain.AccessTokenRepository;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@Component
public class AuthorizationFilter implements GlobalFilter {

    private final WebClient.Builder webClientBuilder;
    private final AccessTokenRepository accessTokenRepository;

    public AuthorizationFilter(WebClient.Builder webClientBuilder, AccessTokenRepository accessTokenRepository) {
        this.webClientBuilder = webClientBuilder;
        this.accessTokenRepository = accessTokenRepository;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // get the route
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);

        // get the metadata
        var metadata = route.getMetadata();
        // check if the route is public
        if (metadata != null && metadata.containsKey("public") && ((boolean) metadata.get("public"))) {
            // if public, continue
            return chain.filter(exchange);
        }

        // get the authorization header from the request
        var authorization = exchange.getRequest()
                .getHeaders()
                .getFirst("Authorization");

        // if JWT is null, return 401
        if (authorization == null) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Authorization header is missing", null);
        }

        // get the JWT token
        var jwt = authorization.replace("Bearer ", "");

        Optional<AccessToken> accessToken = accessTokenRepository.findFirstByAccessToken(jwt);
        if (accessToken.isEmpty() || !accessToken.get().isActive()) {
            return onError(exchange, HttpStatus.UNAUTHORIZED, "Invalid access token", null);
        }


        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange,
                               HttpStatus httpStatus,
                               String message,
                               Map<String, String> errors) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders()
                .add("Content-Type", "application/json");
        DataBufferFactory bufferFactory = response.bufferFactory();
        String errorsString = errors == null ? "" : errors.toString();

        StringBuilder responseData = new StringBuilder();
        // add errors only if there are any
        if (!errorsString.isEmpty()) {
            responseData.append("{\"message\": \"")
                    .append(message)
                    .append("\", \"errors\": ")
                    .append(errors)
                    .append("}");
        } else {
            responseData.append("{\"message\": \"")
                    .append(message)
                    .append("\"}");
        }

        return response.writeWith(Mono.just(bufferFactory.wrap((responseData.toString()).getBytes())));
    }
}
