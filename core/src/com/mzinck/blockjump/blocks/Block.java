package com.mzinck.blockjump.blocks;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class Block {

    private TextureRegion blockCurrent, blockSleep, blockAwake;
    private Rectangle     blockRectangle;

    public Block(TextureRegion blockSleep, TextureRegion blockAwake,
            Rectangle blockRectangle) {
        this.blockSleep = blockSleep;
        this.blockAwake = blockAwake;
        this.blockRectangle = blockRectangle;
        this.blockCurrent = blockSleep;
    }

    public TextureRegion getBlockSleep() {
        return blockSleep;
    }

    public void setBlockSleep(TextureRegion blockSleep) {
        this.blockSleep = blockSleep;
    }

    public TextureRegion getBlockAwake() {
        return blockAwake;
    }

    public void setBlockAwake(TextureRegion blockAwake) {
        this.blockAwake = blockAwake;
    }

    public Rectangle getBlockRectangle() {
        return blockRectangle;
    }

    public void setBlockRectangle(Rectangle blockRect) {
        this.blockRectangle = blockRect;
    }

    public TextureRegion getBlockCurrent() {
        return blockCurrent;
    }

    public void setBlockCurrent(TextureRegion blockCurrent) {
        this.blockCurrent = blockCurrent;
    }

}
