package com.github.spacemex.deliveryquesting.config;

import com.github.spacemex.yml.YamlConfigTemplateWriter;

import java.io.File;
import java.nio.file.Path;

public final class CommonConfig {

    private CommonConfig() {
    }

    public static void generate(Path path) {
        File yamlFile = path.toFile();

        File parent = yamlFile.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }

        YamlConfigTemplateWriter writer =
                new YamlConfigTemplateWriter(yamlFile);

        writer.add(
                "minComputerLevel",
                10,
                "The level when computers should be usable. Min: 0, Max: "
                        + Integer.MAX_VALUE
        );

        writer.write();
    }
}