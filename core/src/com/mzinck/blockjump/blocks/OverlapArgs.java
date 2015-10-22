package com.mzinck.blockjump.blocks;

public class OverlapArgs {

    private PlayerState state;
    private boolean     blockIsFalling;
    private Block       block;

    public OverlapArgs(PlayerState state, boolean blockIsFalling, Block block) {
        this.state = state;
        this.blockIsFalling = blockIsFalling;
        this.block = block;
    }

    public PlayerState getState() {
        return state;
    }

    public boolean isBlockIsFalling() {
        return blockIsFalling;
    }

    public Block getBlock() {
        return block;
    }

}
