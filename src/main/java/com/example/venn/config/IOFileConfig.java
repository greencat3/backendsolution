package com.example.venn.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class IOFileConfig {
    @Bean("outputFile")
    public File getOutputFile(@Value("${loadevent.output.file}") String filepath) {
        var outputFile = new File(filepath);
        return outputFile;
    }

    @Bean("inputFile")
    public File getInputFile(@Value("${loadevent.input.file}") String filepath) {
        var inputFile = new File(filepath);
        return inputFile;
    }
}
