package com.mzinck.blockjump.blocks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.TimeUtils;
import com.mzinck.blockjump.Constants;
import com.mzinck.blockjump.GameScreen;
import com.mzinck.blockjump.lava.Lava;
import com.mzinck.blockjump.objects.Player;

public class BlockLogic {

    private ArrayList<Block> blocksMoving     = new ArrayList<Block>(); // Redo
                                                                        // this
                                                                        // so
                                                                        // that
                                                                        // it
                                                                        // updates
                                                                        // in
                                                                        // the
                                                                        // Block.java
                                                                        // for
                                                                        // eachblock
    private ArrayList<Block> blocksStationary = new ArrayList<Block>();
    private ArrayList<Block> blocksUnderLava  = new ArrayList<Block>();
    private OverlapArgs[]    args             = new OverlapArgs[4];
    private TextureRegion[]  blockTextures;
    private Player           player;
    private Rectangle        lastSpawn, plr;
    private Lava             lava;
    private boolean          fall;
    private long             lastDropTime;

    public BlockLogic(Player player, Lava lava, TextureRegion[] blockTextures) {
        this.player = player;
        this.lava = lava;
        this.blockTextures = blockTextures;
    }

    /**
     * Collision and block updating method. TO-DO: Clean and seperate collision
     * and block updating. Make Blocks update in the Block.java class
     * 
     * @param twice
     *            Determines whether the thread is updating for a second time.
     *            Updates a second time if the player is halfway across the
     *            screen.
     */
    public void update(boolean twice) {
        GameScreen.cameraFallSpeed = player.getFallSpeed();
        plr = player.getPlayerRectangle();
        Block bl;

        if (twice == false) {
            fall = true;
            player.setMaxSpeedLeft(20);
            player.setMaxSpeedRight(20);
        } else {
            int x = 0;
            if (player.getX()
                    + player.getDimension() > Constants.SCREEN_WIDTH) {
                x = player.getX() - Constants.SCREEN_WIDTH;
            } else if (player.getX() < 0) {
                x = player.getX() + Constants.SCREEN_WIDTH;
            }
            plr.x = x;
        }

        for (int z = 0; z < blocksMoving.size(); z++) {
            boolean moving = true;
            bl = blocksMoving.get(z);
            bl.setBlockCurrent(bl.getBlockSleep());

            if (twice == false && moving == true) {
                if (bl.getBlockRectangle().y - 5 > Constants.BASE_HEIGHT) {
                    // bl.y -= 200 * Gdx.graphics.getDeltaTime();
                    bl.getBlockRectangle().y -= 5;
                } else {
                    bl.getBlockRectangle().y = Constants.BASE_HEIGHT;
                    blocksStationary.add(bl);
                    blocksMoving.remove(bl);
                }
            }

            // for(int i = 0; i < blocksMoving.size(); i++) {
            // Rectangle zv = blocksMoving.get(i).getBlockRectangle();
            // if(bl.getBlockRectangle().overlaps(zv) && bl.getBlockRectangle()
            // != zv) {
            // moving = false;
            // blocksMoving.remove(bl);
            // blocksStationary.add(bl);
            // }
            // }

            for (int i = 0; i < blocksStationary.size(); i++) {
                Block zv = blocksStationary.get(i);
                if (bl.getBlockRectangle().overlaps(zv.getBlockRectangle())
                        && bl != zv) {
                    moving = false;
                    bl.getBlockRectangle().setY(zv.getBlockRectangle().getY()
                            + zv.getBlockRectangle().getHeight() + 1);
                    blocksMoving.remove(bl);
                    blocksStationary.add(bl);
                }
            }
        }

        Iterator<Block> iterator = blocksStationary.iterator();
        while (iterator.hasNext()) {
            Block block = iterator.next();
            block.setBlockCurrent(block.getBlockSleep());
            if (block.getBlockRectangle().getY() + 1000 < lava.getHeight()) {
                blocksUnderLava.add(block);
                iterator.remove();
            }
        }

        if (twice == false) {
            if (player.checkBoth()) {
                updatePlayerCollision(twice);
                update(true);
            } else {
                updatePlayerCollision(twice);
            }
        } else {
            updatePlayerCollision(twice);
        }
    }

    public void overLapCheck(Block block, Rectangle playerRectangle,
            boolean blockIsFalling, String pState) {
        if (block.getBlockRectangle().overlaps(playerRectangle)) {
            switch (pState) {
                case "sides":
                    if (overLapsRightSide(block.getBlockRectangle(),
                            playerRectangle)
                            && !isOnBlock(block.getBlockRectangle(),
                                    playerRectangle)) {
                        args[PlayerState.OVERLAPS_RIGHT
                                .getSlot()] = new OverlapArgs(
                                        PlayerState.OVERLAPS_RIGHT,
                                        blockIsFalling, block);
                    } else if (overLapsLeftSide(block.getBlockRectangle(),
                            playerRectangle)
                            && !isOnBlock(block.getBlockRectangle(),
                                    playerRectangle)) {
                        args[PlayerState.OVERLAPS_LEFT
                                .getSlot()] = new OverlapArgs(
                                        PlayerState.OVERLAPS_LEFT,
                                        blockIsFalling, block);
                    }
                    break;
                case "top":
                    if (isOnBlock(block.getBlockRectangle(), playerRectangle)) {
                        if (args[PlayerState.OVERLAPS_TOP.getSlot()] == null) {
                            args[PlayerState.OVERLAPS_TOP
                                    .getSlot()] = new OverlapArgs(
                                            PlayerState.OVERLAPS_TOP,
                                            blockIsFalling, block);
                        }
                    }
                    break;
                case "bottom":
                    if (isUnderBlock(block.getBlockRectangle(),
                            playerRectangle)) {
                        if (args[PlayerState.OVERLAPS_BOTTOM
                                .getSlot()] == null) {
                            args[PlayerState.OVERLAPS_BOTTOM
                                    .getSlot()] = new OverlapArgs(
                                            PlayerState.OVERLAPS_BOTTOM,
                                            blockIsFalling, block);
                        }
                    }
                    break;
            }

            block.setBlockCurrent(block.getBlockAwake());
        }
    }

    public void overLapCheck(String state) {
        for (Block b : blocksStationary) {
            if (state.equals("bottom") || state.equals("top")) {
                if ((args[PlayerState.OVERLAPS_LEFT.getSlot()] != null)) {
                    if (args[PlayerState.OVERLAPS_LEFT.getSlot()]
                            .getBlock() == b) {
                        continue;
                    }
                }
                if (args[PlayerState.OVERLAPS_RIGHT.getSlot()] != null) {
                    if (args[PlayerState.OVERLAPS_RIGHT.getSlot()]
                            .getBlock() == b) {
                        continue;
                    }
                }
            }
            overLapCheck(b, plr, false, state);
        }
        for (Block b : blocksMoving) {
            if (state.equals("bottom") || state.equals("top")) {
                if ((args[PlayerState.OVERLAPS_LEFT.getSlot()] != null)) {
                    if (args[PlayerState.OVERLAPS_LEFT.getSlot()]
                            .getBlock() == b) {
                        continue;
                    }
                }
                if (args[PlayerState.OVERLAPS_RIGHT.getSlot()] != null) {
                    if (args[PlayerState.OVERLAPS_RIGHT.getSlot()]
                            .getBlock() == b) {
                        continue;
                    }
                }
            }
            overLapCheck(b, plr, true, state);
        }
    }

    public void updatePlayerCollision(boolean twice) {
        overLapCheck("sides");
        if (args[PlayerState.OVERLAPS_LEFT.getSlot()] != null) {
            player.setMaxSpeedRight((int) 0);
            player.setX((int) (args[PlayerState.OVERLAPS_LEFT.getSlot()]
                    .getBlock().getBlockRectangle().x - player.getDimension()
                    + 1));
        }
        if (args[PlayerState.OVERLAPS_RIGHT.getSlot()] != null) {
            player.setMaxSpeedLeft((int) 0);
            player.setX((int) (args[PlayerState.OVERLAPS_RIGHT.getSlot()]
                    .getBlock().getBlockRectangle().x
                    + args[PlayerState.OVERLAPS_RIGHT.getSlot()].getBlock()
                            .getBlockRectangle().width
                    - 1));
        }

        overLapCheck("bottom");
        if (args[PlayerState.OVERLAPS_BOTTOM.getSlot()] != null) {
            if (player.getY() == Constants.BASE_HEIGHT) {
                player.kill();
            }
            player.setJump(Constants.JUMP_LENGTH + 1);
            player.setY((int) args[PlayerState.OVERLAPS_BOTTOM.getSlot()]
                    .getBlock().getBlockRectangle().getY()
                    - player.getDimension());
        }

        overLapCheck("top");
        if (args[PlayerState.OVERLAPS_TOP.getSlot()] != null) {
            if (args[PlayerState.OVERLAPS_BOTTOM.getSlot()] == null) {
                GameScreen.cameraFallSpeed = 5;
                fall = false;
                if (args[PlayerState.OVERLAPS_TOP.getSlot()]
                        .isBlockIsFalling() == true) {
                    fall = true;
                    player.setFallSpeed(5);
                }
                player.setY((int) (args[PlayerState.OVERLAPS_TOP.getSlot()]
                        .getBlock().getBlockRectangle().y
                        + args[PlayerState.OVERLAPS_TOP.getSlot()].getBlock()
                                .getBlockRectangle().height
                        - 1));
                if (player.getJump() > Constants.JUMP_LENGTH) {
                    player.setJump(0);
                    player.setJumping(false);
                }
                player.setWaitTime(
                        player.getWaitTime() == 0 ? 5 : player.getWaitTime());
            } else if (args[PlayerState.OVERLAPS_TOP.getSlot()]
                    .getBlock() != args[PlayerState.OVERLAPS_BOTTOM.getSlot()]
                            .getBlock()
                    && args[PlayerState.OVERLAPS_BOTTOM.getSlot()]
                            .isBlockIsFalling()) {
                player.kill();
            }
        }

        player.setFalling(fall);

        for (int i = 0; i < args.length; i++) {
            args[i] = null;
        }
    }

    public boolean isUnderBlock(Rectangle block, Rectangle playerRectangle) {
        return ((playerRectangle.y + player.getDimension() >= block.y)
                && (playerRectangle.y + player.getDimension() <= block.y
                        + player.getJumpSpeed() + 5));
    }

    public boolean isOnBlock(Rectangle block, Rectangle playerRectangle) {
        return ((block.y + block.height >= playerRectangle.y)
                && (block.y + (block.height - player.getFallSpeed()
                        - 2) <= playerRectangle.y));
    }

    public boolean overLapsRightSide(Rectangle block,
            Rectangle playerRectangle) {
        return ((block.x + block.width >= playerRectangle.x)
                && (block.x + (block.width
                        - player.getMaxSpeedLeft()) <= playerRectangle.x));
    }

    public boolean overLapsLeftSide(Rectangle block,
            Rectangle playerRectangle) {
        return ((block.x <= playerRectangle.x + player.getDimension())
                && (block.x + player.getMaxSpeedRight() >= playerRectangle.x
                        + player.getDimension()));
    }

    public void spawnBlock() {
        Rectangle rect = new Rectangle();
        int rand = MathUtils.random(Constants.SCREEN_WIDTH - 153);
        rect.x = rand;
        if (lastSpawn != null) {
            rect.y = lastSpawn.y + 170;
        } else {
            rect.y = player.getY() + 1300;
        }
        rand = MathUtils.random(100);
        rect.width = rand > 50 ? 102 : 153;
        rect.height = rand > 50 ? 108 : 162;

        if (lastSpawn != null) {
            if (lastSpawn.x < 360) {
                int xSpawn = (int) (lastSpawn.x + 153 > 360 ? lastSpawn.x + 160
                        : 360);
                rect.x = MathUtils.random(xSpawn, Constants.SCREEN_WIDTH - 153);
            } else {
                int xSpawn = (int) (lastSpawn.x < 360 ? lastSpawn.x - 10 : 360);
                rect.x = MathUtils.random(0, xSpawn);
            }
        }

        Block block = new Block(blockTextures[0], blockTextures[1], rect);

        blocksMoving.add(block);
        lastDropTime = TimeUtils.millis();
        lastSpawn = new Rectangle(rect.x, rect.y, rect.width, rect.height);
    }

    public long getLastDropTime() {
        return lastDropTime;
    }

    public ArrayList<Block> getBlocksMoving() {
        return blocksMoving;
    }

    public ArrayList<Block> getBlocksUnderLava() {
        return blocksUnderLava;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public ArrayList<Block> getBlocksStationary() {
        return blocksStationary;
    }

    public void setBlocksStationary(ArrayList<Block> blocksStationary) {
        this.blocksStationary = blocksStationary;
    }

}
