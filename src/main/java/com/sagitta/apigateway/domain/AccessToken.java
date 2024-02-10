package com.sagitta.apigateway.domain;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("AccessToken")
@RequiredArgsConstructor
@AllArgsConstructor
@Data
@EntityScan
@Builder
public class AccessToken {
    @Id
    private String accessToken;
    private boolean isActive;
}
