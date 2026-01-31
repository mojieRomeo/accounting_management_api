package top.swjtuhc.accounting_management_api.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //当浏览器访问http://localhost:9090/avatar/xxx.png，会自动去/Users/luojunjie/Desktop/找图片
        registry.addResourceHandler("/avatar/**")
                .addResourceLocations("file:/Users/luojunjie/Desktop/");
    }
}

