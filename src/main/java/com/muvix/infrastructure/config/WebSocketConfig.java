package com.muvix.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.muvix.application.port.in.logistic.ChatCoordinatorUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtDecoder jwtDecoder;
    private final ChatCoordinatorUseCase chat;
    private final ObjectMapper objectMapper;
    private final String[] allowedOrigins;

    public WebSocketConfig(JwtDecoder jwtDecoder,
                           ChatCoordinatorUseCase chat,
                           ObjectMapper objectMapper,
                           @Value("${muvix.chat.allowed-origins:http://localhost:3000}") String allowedOrigins){
        this.jwtDecoder=jwtDecoder;
        this.chat=chat;
        this.objectMapper=objectMapper;
        this.allowedOrigins=allowedOrigins.split(",");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("/ws-chat").setAllowedOriginPatterns(allowedOrigins).withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){
        registry.setApplicationDestinationPrefixes("/app");registry.enableSimpleBroker("/topic");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration){
        registration.interceptors(new AuthenticatedChatInterceptor(jwtDecoder,chat,objectMapper));
    }

    static final class AuthenticatedChatInterceptor implements ChannelInterceptor {
        private static final Pattern ORDER_TOPIC=Pattern.compile("/topic/orders/(\\d+)");
        private static final Pattern OFFER_TOPIC=Pattern.compile("/topic/offers/(\\d+)");
        private static final Pattern ORDER_SEND=Pattern.compile("/app/chat/orders/(\\d+)/messages");
        private static final Pattern OFFER_SEND=Pattern.compile("/app/chat/offers/(\\d+)/messages");
        private final JwtDecoder decoder;
        private final ChatCoordinatorUseCase chat;
        private final ObjectMapper mapper;

        AuthenticatedChatInterceptor(JwtDecoder decoder,ChatCoordinatorUseCase chat,ObjectMapper mapper){
            this.decoder=decoder;
            this.chat=chat;
            this.mapper=mapper;
        }

        @Override
        public Message<?> preSend(Message<?> message,MessageChannel channel){
            StompHeaderAccessor a=StompHeaderAccessor.wrap(message);
            if(a.getCommand()==StompCommand.CONNECT){
                String auth=a.getFirstNativeHeader("Authorization");
                if(auth==null||!auth.startsWith("Bearer "))throw new IllegalArgumentException("Bearer access token is required for chat");
                Jwt jwt=decoder.decode(auth.substring(7));
                if(!"access".equals(jwt.getClaimAsString("scope")))throw new IllegalArgumentException("Access token required");
                var authorities=jwt.getClaimAsStringList("roles")==null?List.<SimpleGrantedAuthority>of():jwt.getClaimAsStringList("roles").stream().map(r->new SimpleGrantedAuthority("ROLE_"+r)).toList();
                a.setUser(new UsernamePasswordAuthenticationToken(jwt.getSubject(),null,authorities));
            }
            if(a.getCommand()==StompCommand.SUBSCRIBE){
                String destination=a.getDestination()==null?"":a.getDestination();Matcher order=ORDER_TOPIC.matcher(destination),offer=OFFER_TOPIC.matcher(destination);
                if(a.getUser()==null||!(order.matches()&&chat.canAccessOrder(Long.valueOf(order.group(1)),Long.valueOf(a.getUser().getName())))&&!(offer.matches()&&chat.canAccessOffer(Long.valueOf(offer.group(1)),Long.valueOf(a.getUser().getName()))))throw new IllegalArgumentException("Not allowed to subscribe to this chat");
            }
            if(a.getCommand()==StompCommand.SEND){
                String destination=a.getDestination()==null?"":a.getDestination();Matcher order=ORDER_SEND.matcher(destination),offer=OFFER_SEND.matcher(destination);
                if(a.getUser()==null||!(order.matches()&&chat.canAccessOrder(Long.valueOf(order.group(1)),Long.valueOf(a.getUser().getName())))&&!(offer.matches()&&chat.canAccessOffer(Long.valueOf(offer.group(1)),Long.valueOf(a.getUser().getName()))))throw new IllegalArgumentException("Only chat participants may send messages");
            }
            return MessageBuilder.createMessage(message.getPayload(),a.getMessageHeaders());
        }
    }
}