package com.batch.practice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Setter
@Getter
@Configuration
public class PropertiesPostProcessor implements EnvironmentPostProcessor {

    private final YamlPropertySourceLoader propertySourceLoader = new YamlPropertySourceLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        List<String> profiles = Arrays.asList(environment.getActiveProfiles());

        Resource databasePropertiesPath = new ClassPathResource("config/database-properties.yml");
        List<PropertySource<?>> databasePropertySource = loadYaml(databasePropertiesPath, profiles);
        databasePropertySource.forEach(data -> environment.getPropertySources().addLast(data));

    }

    private List<PropertySource<?>> loadYaml(Resource propertyPath, List<String> profiles) {
        if(!propertyPath.exists())  {
            throw new IllegalArgumentException("Resource " + propertyPath + " does not exist!!!");
        }

        try {
            List<PropertySource<?>> propertySources = this.propertySourceLoader.load(propertyPath.getDescription(), propertyPath);

            return propertySources.stream()
                    .filter(data -> !data.containsProperty("spring.profiles") ||
                            profiles.contains(Objects.requireNonNull(data.getProperty("spring.profiles")).toString()) ||
                            Arrays.asList(Objects.requireNonNull(data.getProperty("spring.profiles")).toString().split(",")).contains(profiles.get(0)))
                    .collect(Collectors.toList());
        } catch (IOException ex)    {
            throw new IllegalArgumentException("Failed to load Yaml Configuration from " + propertyPath, ex);
        }
    }
}
