package de.hype.eggsentials.client.common.mclibraries;

import de.hype.eggsentials.client.common.client.Eggsentials;

public abstract class CustomItemTexture {
    public static int customTextureId = 0;
    public String nameSpace = null;
    public String renderTextureId = null;
    public int textureId = customTextureId++;

    /**
     * @param nameSpace
     * @param renderTextureId keep in mind that the texture probably needs to be in Texturepackfolder/assets/eggsentials/textures/gui/sprites/likeyouwant
     */
    public CustomItemTexture(String nameSpace, String renderTextureId) {
        this.nameSpace = nameSpace;
        this.renderTextureId = renderTextureId;
        addToPool();
    }

    /**
     * @param renderTextureId only id NO NAMEPSACE!
     */
    public CustomItemTexture(String renderTextureId) {
        this.nameSpace = "eggsentials";
        this.renderTextureId = renderTextureId;
        addToPool();
    }

    protected int addToPool() {
        Eggsentials.customItemTextures.put(textureId, this);
        return textureId;
    }

    public CustomItemTexture removeFromPool() {
        return Eggsentials.customItemTextures.remove(textureId);
    }

    /**
     * @param itemName
     * @param nbt
     * @param item
     * @return
     */
    public abstract boolean isItem(String itemName, String nbt, Object item);
}
