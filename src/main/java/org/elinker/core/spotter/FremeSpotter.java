package org.elinker.core.spotter;

import eu.freme.common.exception.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;

@Component
@RequiredArgsConstructor
public class FremeSpotter implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${freme.ner.dictionary:}")
    private String dictionary = "";

    private Boolean hasBuilt = false;

    private Trie.TrieBuilder builder;

    private Trie trie;

    private void buildDictionary() {

        if (dictionary == null || dictionary.isEmpty()) {
            trie = builder.build();
            return;
        }


        builder = Trie.builder().removeOverlaps().caseInsensitive();

        try (Stream<String> stream = Files.lines(Paths.get(dictionary))) {
            stream.forEach(s -> {
                builder.addKeyword(s);
            });
        } catch (IOException e) {
            throw new InternalServerErrorException(String.format("It was not possible to read the spotter dictionary." +
                    " Please check if the path is correct (%s)", dictionary));
        }
        trie = builder.build();
    }

    public void addKey(String key) {

        if (dictionary == null || dictionary.isEmpty()) {
            return;
        }

        try {
            builder.addKeyword(key);
            trie = builder.build();
            Files.write(Paths.get(dictionary), key.concat(System.lineSeparator()).getBytes(UTF_8), APPEND);
        } catch (IOException e) {
            throw new InternalServerErrorException("It was not possible to add a key into the spotter dictionary.");
        }

    }

    public Collection<Emit> parseText(String text) {
        return trie.parseText(text);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (!hasBuilt) {
            buildDictionary();
        }

    }

}
