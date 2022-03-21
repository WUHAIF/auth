package com.wuhf.oauth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wuhf.oauth.adapter.ClientDetailsAdapter;
import com.wuhf.oauth.dao.OauthClientDetailsMapper;
import com.wuhf.oauth.entity.OauthClientDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @Author: wuhaifeng
 * @DateTime: 2022/3/21 11:00
 * @Description: TODO
 */
@Slf4j
@Service("oauthClientDetailsService")
public class OauthClientDetailsService extends ServiceImpl<OauthClientDetailsMapper, OauthClientDetails> implements ClientDetailsService {

    @Resource
    private RedisTemplate<String, OauthClientDetails> redisTemplate;
    private String prefix = "ClientDetails:";

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        // 优先从redis缓存中获取，不存在则从数据库中获取
        OauthClientDetails oauthClientDetails = redisTemplate.opsForValue().get(prefix + clientId);
        if(oauthClientDetails == null){
            LambdaQueryWrapper<OauthClientDetails> query = new LambdaQueryWrapper<>();
            query.eq(OauthClientDetails::getAppKey, clientId);
            oauthClientDetails = super.getOne(query);
            if(oauthClientDetails == null){
                return null;
            }

            redisTemplate.opsForValue().set(prefix + clientId, oauthClientDetails, 1, TimeUnit.HOURS);
        }
        return new ClientDetailsAdapter(oauthClientDetails);
    }
}

