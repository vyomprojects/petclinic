package tk.puncha.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

  @Bean
  Properties exceptionMappings() {
    Properties mappings = new Properties();
    mappings.put("OopsException", "exception/oops");
    return mappings;
  }

  @Bean
  public HandlerExceptionResolver handlerExceptionResolver() {
    SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();
    resolver.setDefaultErrorView("exception/default");
    resolver.setExceptionAttribute("exception");
    resolver.setExceptionMappings(exceptionMappings());
    return resolver;
  }

  @Override
  public void configureViewResolvers(ViewResolverRegistry registry) {
    super.configureViewResolvers(registry);
    registry.jsp("/WEB-INF/jsp/", ".jsp");
  }

  @Override
  public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
    exceptionResolvers.add(handlerExceptionResolver());
  }

  @Bean
  public DataSource dataSource() {
    DataSource dataSource = new EmbeddedDatabaseBuilder()
        .generateUniqueName(true)
        .ignoreFailedDrops(true)
        .setScriptEncoding("UTF-8")
        .addScript("/db/hsqldb/initDB.sql")
        .addScript("/db/hsqldb/populateDB.sql")
        .build();

    return dataSource;
  }
}