package com.sivateja.studycollabration.websocket;
import com.sivateja.studycollabration.Security.CustomUserDetailsService;
import com.sivateja.studycollabration.Security.JwtConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor; // Correct class
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtConfig jwtConfig;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Sets prefix for the "Server -> Client" messages
        registry.enableSimpleBroker("/topic");
        // Sets prefix for the "Client -> Server" messages
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                // Correctly extract the STOMP headers from the message
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // Extract the "Authorization" header sent from React's connectHeaders
                    String authHeader = accessor.getFirstNativeHeader("Authorization");

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        String token = authHeader.substring(7);
                        try {
                            String username = jwtConfig.getUserName(token);
                            UserDetails user = userDetailsService.loadUserByUsername(username);

                            if (jwtConfig.valid(token, user)) {
                                // Important: This populates the Principal in your controllers
                                UsernamePasswordAuthenticationToken auth =
                                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                                accessor.setUser(auth);
                            } else {
                                throw new MessageDeliveryException("Invalid JWT Token");
                            }
                        } catch (Exception e) {
                            throw new MessageDeliveryException("Authentication failed: " + e.getMessage());
                        }
                    } else {
                        // This will trigger the 'onStompError' callback in your React client
                        throw new MessageDeliveryException("Missing Authorization Header");
                    }
                }
                return message;
            }
        });
    }
}