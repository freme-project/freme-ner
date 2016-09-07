package org.elinker.core.api.java;

public class ResourceTestFiles {

    protected String getContent(String path) throws Exception {
        java.net.URL url = this.getClass().getClassLoader().getResource(path);
        java.nio.file.Path resPath = java.nio.file.Paths.get(url.toURI());
        return new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
    }


}
