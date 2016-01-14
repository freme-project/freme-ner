package org.elinker.core.api.java.serialize;

/**
 *
 * @author Milan Dojchinovski <milan.dojchinovski@fit.cvut.cz>
 * http://dojchinovski.mk
 */
public class EntityMention {
    private int beginIndex;
    private int endIndex;
    private String mention;

    /**
     * @return the beginIndex
     */
    public int getBeginIndex() {
        return beginIndex;
    }

    /**
     * @param beginIndex the beginIndex to set
     */
    public void setBeginIndex(int beginIndex) {
        this.beginIndex = beginIndex;
    }

    /**
     * @return the endIndex
     */
    public int getEndIndex() {
        return endIndex;
    }

    /**
     * @param endIndex the endIndex to set
     */
    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    /**
     * @return the mention text
     */
    public String getMention() {
        return mention;
    }

    /**
     * @param mention the mention text to set
     */
    public void setMention(String mention) {
        this.mention = mention;
    }
}
