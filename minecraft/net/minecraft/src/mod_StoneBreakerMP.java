package net.minecraft.src;

import java.util.LinkedHashSet;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;

public class mod_StoneBreakerMP extends BaseModMp {
	class Position {
		int x;
		int y;
		int z;

		public Position(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public Position subtract(Position position) {
			return new Position(position.x - x, position.y - y, position.z - z);
		}

		public Position addVector(int x2, int y2, int z2) {
			return new Position(x2 + x, y2 + y, z2 + z);
		}

	    public String toString()
	    {
	        return (new StringBuilder()).append("(").append(x).append(", ").append(y).append(", ").append(z).append(")").toString();
	    }

	    @Override
	    public int hashCode() {
	    	return 13 * 13 * x + 13 * y + z;
	    }

	    @Override
	    public boolean equals(Object obj) {
	    	if(obj == null) return false;
	    	if(obj instanceof Position) {
	    		Position pos = (Position)obj;
	    		if(x == pos.x && y == pos.y && z == pos.z) {
	    			return true;
	    		}
	    	}
	    	return false;
	    }
	}
	public static KeyBinding key_stonebreak = null;
	public static int prev_i;
	public static int prev_j;
	public static int prev_k;
	public static int blockId;
	public static int metadata;
	public static int prev_blockHitWait;
	public static Set<Position> vectors = new LinkedHashSet();
	public static Set<Position> positions = new LinkedHashSet();

	public static final int mode_off = 0;
	public static final int mode_line = 1;
	public static final int mode_tunnel = 2;
	public static final int mode_front_upper = 3;
	public static final int mode_front_under = 4;
	public static final int mode_front = 5;
	public static final int mode_upper = 6;
	public static final int mode_under = 7;
	public static final int mode_horizontal = 8;
	public static final int mode_all = 9;
	public static final int mode_max = 10;

	@MLProp(min = 0, max = 9)
	public static int mode = mode_line;


	public static Set<Integer> targetIDs = new LinkedHashSet();

	public static boolean debugmode = true;

	@MLProp(info = "toggle mode key(default:50 = 'M')")
	public static int mode_key = 50;

	public static int key_push_times = 0;

	public static boolean bInit = false;

	public static int breakcount = 0;

	public static int breaklimit = 0;

	public static boolean bObfuscate = false;
	private boolean[] mode_allow = new boolean[10];

	public static final int cmd_break = 0;
	public static final int cmd_mode = 1;
	public static final int cmd_target = 2;
	public static final int cmd_limit = 3;
	public static final int cmd_itembreak = 4;

	public static boolean bInitMode = false;
	public static boolean bInitTarget = false;
	public static boolean bInitLimit = false;

	@Override
	public String getVersion() {
		return "[1.2.3] StoneBreakerMP 0.0.1";
	}

	@Override
	public void load() {
		System.out.print("StoneBreakerMP target = ");
		System.out.println(targetIDs);
		ModLoader.setInGameHook(this, true, false);
	}

	public void printMode(Minecraft minecraft) {
		switch(mode) {
		case mode_off:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : OFF");
			break;
		case mode_line:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : LINE");
			break;
		case mode_tunnel:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : TUNNEL");
			break;
		case mode_front_upper:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : FRONT_UPPER");
			break;
		case mode_front_under:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : FRONT_UNDER");
			break;
		case mode_front:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : FRONT");
			break;
		case mode_upper:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : UPPER");
			break;
		case mode_under:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : UNDER");
			break;
		case mode_horizontal:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : HORIZONTAL");
			break;
		case mode_all:
			minecraft.ingameGUI.addChatMessage("StoneBreaker mode : ALL");
			break;
		}
	}

	@Override
	public boolean onTickInGame(float f, Minecraft minecraft) {
		if(minecraft.isMultiplayerWorld() == false) {
			return false;
		}

		if(Keyboard.isKeyDown(mode_key)) {
			key_push_times++;
		} else if(key_push_times > 0) {
			while(true) {
				mode = (mode + 1) % mode_max;
				if(mode_allow[mode]) {
					break;
				}
			}
			key_push_times = 0;
			printMode(minecraft);
		}


		if(bInit == false && bInitMode && bInitTarget && bInitLimit) {
			printMode(minecraft);
			String s = "StoneBreaker Mode = [";
			if(mode_allow[0]) s += "OFF";
			if(mode_allow[1]) s += " LINE";
			if(mode_allow[2]) s += " TUNNEL";
			if(mode_allow[3]) s += " FRONT_UPPER";
			if(mode_allow[4]) s += " FRONT_UNDER";
			if(mode_allow[5]) s += " FRONT";
			if(mode_allow[6]) s += " UPPER";
			if(mode_allow[7]) s += " UNDER";
			if(mode_allow[8]) s += " HORIZONTAL";
			if(mode_allow[9]) s += " ALL";
			s += "]";
			minecraft.ingameGUI.addChatMessage(s);

			s = "StoneBreaker target = ";
			s += targetIDs;
			minecraft.ingameGUI.addChatMessage(s);

			s = "StoneBreaker limit = ";
			s += breaklimit;
			minecraft.ingameGUI.addChatMessage(s);

			bInit = true;
		}

		boolean breakflag = false;

		int blockHitWait = getBlockHitWait(minecraft);
		if(blockHitWait == 5 && blockHitWait != prev_blockHitWait) {
			System.out.println(blockHitWait);
			breakflag = true;
		}
		prev_blockHitWait = blockHitWait;

		if(breakflag) {
			if(targetIDs.contains(blockId)) {
				System.out.println("startBreak");
				startBreak(minecraft);
			} else {
				if(debugmode) {
					System.out.print("BlockId ");
					System.out.print(blockId);
					System.out.print(" not in targetIDs ");
					System.out.println(targetIDs);
				}
			}
		}

		if(positions.size() > 0) {
			continueBreak(minecraft);
			return true;
		}


		//if(Mouse.isButtonDown(0) == false) {
		//	return true;
		//}
		if (minecraft.objectMouseOver == null) {
			return true;
		}

		if (minecraft.objectMouseOver.typeOfHit == EnumMovingObjectType.TILE) {
			prev_i = minecraft.objectMouseOver.blockX;
			prev_j = minecraft.objectMouseOver.blockY;
			prev_k = minecraft.objectMouseOver.blockZ;
			blockId = minecraft.theWorld.getBlockId(prev_i, prev_j, prev_k);
			Block block = Block.blocksList[blockId];
			metadata = minecraft.theWorld.getBlockMetadata(prev_i, prev_j, prev_k);
			Material material = minecraft.theWorld.getBlockMaterial(prev_i, prev_j, prev_k);
			return true;
		}

		return true;
	}

	private int getBlockHitWait(Minecraft minecraft) {
		int blockHitWait = 0;

		if(bObfuscate) {
			try {
				if(minecraft.playerController.isNotCreative()) {
					// blockHitWait obfuscate i
					blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerMP.class, minecraft.playerController, "i");
				}
				else {
					// blockHitWait obfuscate i
					blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerCreative.class, minecraft.playerController, "c");
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		} else {
			try {
				if(minecraft.playerController.isNotCreative()) {
					// blockHitWait obfuscate i
					blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerMP.class, minecraft.playerController, "blockHitDelay");
				}
				else {
					// field_35647_c obfuscate c
					blockHitWait = (Integer) ModLoader.getPrivateValue(PlayerControllerCreative.class, minecraft.playerController, "field_35647_c");
				}
			} catch (IllegalArgumentException e) {
				bObfuscate =true;
				e.printStackTrace();
			} catch (SecurityException e) {
				bObfuscate =true;
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				bObfuscate =true;
				e.printStackTrace();
			}
		}

		return blockHitWait;
	}

	private void continueBreak(Minecraft minecraft) {
		if(debugmode) {
			System.out.print("continueBreak start ");
			System.out.println(positions);
		}
		int i = 0;
		Set<Position> oldPositions = new LinkedHashSet();
		Set<Position> newPositions = new LinkedHashSet();
		oldPositions.addAll(positions);

		int breakOnce = 1;

		// ˆê“x‚É‰ó‚·‚Ì‚Í‚P‚UŒÂ‚Ü‚Å‚Æ‚·‚é
		if(oldPositions.size() < breakOnce) {
			int j = 0;
			// ‚P‚UŒÂ–ÚˆÈ~‚ÍŽŸ‰ñ
			for(Position position : oldPositions) {
				if(j >= breakOnce) {
					newPositions.add(position);
				}
				j++;
			}

			j = 0;
			// Žü•ÓƒuƒƒbƒN‚ðŽŸ‰ñ‚É—\–ñ
			for(Position position : oldPositions) {
				if(j >= breakOnce) {
					break;
				}
				j++;
				for(Position new_pos : addNextBreakBlocks(minecraft, position)) {
					if(positions.contains(new_pos) == false) {
						newPositions.add(new_pos);
					}
				}
			}

			j = 0;
			// —\–ñÏ‚ÝƒuƒƒbƒN‚ð”j‰ó
			for(Position position : oldPositions) {
				if(j >= breakOnce) {
					break;
				}
				j++;
				// ƒc[ƒ‹”j‘¹‚ÍI—¹
				if(breakBlock(minecraft, position) == false) {
					positions.clear();
					return;
				}
				// ˆêŠ‡”j‰óãŒÀ‚ÍI—¹
				if(breaklimit > 0 && breakcount >= breaklimit) {
					positions.clear();
					breakcount = 0;
					return;
				}
			}
		}
		// ‚·‚×‚Ä‰ó‚·
		else {
			// Žü•ÓƒuƒƒbƒN‚ðŽŸ‰ñ‚É—\–ñ
			for(Position position : oldPositions) {
				for(Position new_pos : addNextBreakBlocks(minecraft, position)) {
					if(positions.contains(new_pos) == false) {
						newPositions.add(new_pos);
					}
				}
			}
			// —\–ñÏ‚ÝƒuƒƒbƒN‚ð”j‰ó
			for(Position position : oldPositions) {
				// ƒc[ƒ‹”j‘¹‚ÍI—¹
				if(breakBlock(minecraft, position) == false) {
					positions.clear();
					return;
				}
				// ˆêŠ‡”j‰óãŒÀ‚ÍI—¹
				if(breaklimit > 0 && breakcount >= breaklimit) {
					positions.clear();
					breakcount = 0;
					return;
				}
			}
		}

		positions = newPositions;
		if(positions.size() > 1) {
			if(debugmode) {
				System.out.print("continueBreak end");
				System.out.println(positions);
			}

		}
		if(debugmode) {
			System.out.print("continueBreak end");
			System.out.println(positions);
		}
	}

	// true : continue break
	// false: stop break
	private boolean breakBlock(Minecraft minecraft, Position position) {
		if(debugmode) {
			System.out.print("breakBlock ");
			System.out.print(position.toString());
			System.out.println(" start");
		}

		if(position.x == 0.0F && position.y == 0.0F && position.z == 0.0F) {
			if(debugmode) System.out.println("breakBlock abnormal");

		}

		// height limitation
		if(position.y < 1 || position.y > 255) {
			if(debugmode) System.out.println("breakBlock skip(height)");
			return true;
		}


		// must equip item tool
        ItemStack itemstack = minecraft.thePlayer.getCurrentEquippedItem();
        if (itemstack == null)
        {
        	if(debugmode) System.out.println("breakBlock skip(itemstack == null)");
        	return false;
        }
        if (itemstack.stackSize == 0)
        {
        	if(debugmode) System.out.println("breakBlock skip(itemstack.stackSize == 0)");
        	return false;
        }
    	if(debugmode) System.out.printf("breakBlock itemstack.itemDamage == %d\n", itemstack.getItemDamage());
		if(Item.itemsList[itemstack.itemID] instanceof ItemTool == false) {
			if(debugmode) System.out.println("breakBlock skip(Item not ItemTool)");
			return false;
		}

		int id = minecraft.theWorld.getBlockId((int)position.x, (int)position.y, (int)position.z);
		if(id != blockId) {
			if(id == Block.grass.blockID && blockId == Block.dirt.blockID) {
			}
			else if(blockId == Block.grass.blockID && id == Block.dirt.blockID) {
			}
			else {
				if(debugmode) System.out.println("breakBlock skip(BlockId)");
				return true;
			}
		}
		Block block = Block.blocksList[id];

		if(block == null) {
			if(debugmode) System.out.println("breakBlock skip(block == null)");
			return true;
		}

        int i = minecraft.theWorld.getBlockMetadata((int)position.x, (int)position.y, (int)position.z);
        //boolean flag = minecraft.theWorld.setBlockWithNotify((int)position.x, (int)position.y, (int)position.z, id);

        //if(flag == false) {
        //	if(debugmode) System.out.println("breakBlock skip(setBlockWithNotify)");
        //	return true;
        //}
		EntityClientPlayerMP player = ((EntityClientPlayerMP)minecraft.thePlayer);

        int currentItem = minecraft.thePlayer.inventory.currentItem;
        player.sendQueue.addToSendQueue(new Packet16BlockItemSwitch(currentItem));

		Packet230ModLoader packet = new Packet230ModLoader();
		packet.modId = getId();
		packet.dataInt = new int[6];
		packet.dataInt[0] = cmd_break;
		packet.dataInt[1] = (int)position.x;
		packet.dataInt[2] = (int)position.y;
		packet.dataInt[3] = (int)position.z;
		packet.dataInt[4] = id;
		packet.dataInt[5] = i;
		ModLoaderMp.sendPacket(this, packet);
		if(debugmode) System.out.printf("[%d] send %d, %d, %d, %d, %d, %d\n", packet.modId, packet.dataInt[0], packet.dataInt[1], packet.dataInt[2], packet.dataInt[3], packet.dataInt[4], packet.dataInt[5]);

    	block.onBlockDestroyedByPlayer(minecraft.theWorld, (int)position.x, (int)position.y, (int)position.z, i);
		player.sendQueue.addToSendQueue(new Packet14BlockDig(2, (int)position.x, (int)position.y, (int)position.z, 0));
        minecraft.playerController.onPlayerDestroyBlock((int)position.x, (int)position.y, (int)position.z, 0);

        itemstack.onDestroyBlock(i, (int)position.x, (int)position.y, (int)position.z, minecraft.thePlayer);

        boolean ret = true;
        if (itemstack.stackSize == 0)
        {
            itemstack.onItemDestroyedByUse(minecraft.thePlayer);
            minecraft.thePlayer.destroyCurrentEquippedItem();
    		Packet230ModLoader packet2 = new Packet230ModLoader();
    		packet2.modId = getId();
    		packet2.dataInt = new int[1];
    		packet2.dataInt[0] = cmd_itembreak;
    		ModLoaderMp.sendPacket(this, packet2);

            ret = false;
        }

        boolean flag1 = minecraft.thePlayer.canHarvestBlock(block);

        if (flag1)
        {
        	block.harvestBlock(minecraft.theWorld, minecraft.thePlayer, (int)position.x, (int)position.y, (int)position.z, i);
        }

		breakcount++;

		if(debugmode) System.out.println("breakBlock end");
		return ret;

	}

	@Override
	public void handlePacket(Packet230ModLoader packet230modloader) {
		switch(packet230modloader.dataInt[0]) {
		case cmd_mode:
			mode_allow = new boolean[packet230modloader.dataInt.length];
			mode_allow[0] = true;
			for(int i = 1; i < packet230modloader.dataInt.length; i++) {
				mode_allow[i] = packet230modloader.dataInt[i] == 1 ? true : false;
			}
			bInitMode = true;
			break;
		case cmd_target:
			String str = packet230modloader.dataString[0];
			String[] tokens = str.split(",");
			for(String token : tokens) {
				targetIDs.add(Integer.parseInt(token.trim()));
			}
			bInitTarget = true;
			break;
		case cmd_limit:
			breaklimit = packet230modloader.dataInt[1];
			bInitLimit = true;
			break;
		}
	}

	public Position getDirection(Minecraft minecraft) {
		Position player_position = new Position((int)minecraft.thePlayer.posX, (int)minecraft.thePlayer.posY, (int)minecraft.thePlayer.posZ);
		Position block_position = new Position(prev_i, prev_j, prev_k);
		Position tmp = player_position.subtract(block_position);
		if(Math.abs(tmp.x) > Math.abs(tmp.z)) {
			if(tmp.x > 0) {
				return new Position(1, 0, 0);
			}
			return new Position(-1, 0, 0);
		}
		else {
			if(tmp.z > 0) {
				return new Position(0, 0, 1);
			}
			return new Position(0, 0, -1);
		}
	}

	public Set<Position> getBackDirections(Minecraft minecraft) {
		Position v = getDirection(minecraft);

		Set<Position> set = new LinkedHashSet();
		if(v.x == 1) {
			set.add(new Position(-1, 0, 1));set.add(new Position(-1, 0, 0));set.add(new Position(-1, 0, -1));
			set.add(new Position(-1, 1, 1));set.add(new Position(-1, 1, 0));set.add(new Position(-1, 1, -1));
			set.add(new Position(-1, -1, 1));set.add(new Position(-1, -1, 0));set.add(new Position(-1, -1, -1));
		} else if(v.x == -1) {
			set.add(new Position(1, 0, 1));set.add(new Position(1, 0, 0));set.add(new Position(1, 0, -1));
			set.add(new Position(1, 1, 1));set.add(new Position(1, 1, 0));set.add(new Position(1, 1, -1));
			set.add(new Position(1, -1, 1));set.add(new Position(1, -1, 0));set.add(new Position(1, -1, -1));
		} else if(v.z == 1) {
			set.add(new Position(1, 0, -1));set.add(new Position(0, 0, -1));set.add(new Position(-1, 0, -1));
			set.add(new Position(1, 1, -1));set.add(new Position(0, 1, -1));set.add(new Position(-1, 1, -1));
			set.add(new Position(1, -1, -1));set.add(new Position(0, -1, -1));set.add(new Position(-1, -1, -1));
		} else if(v.z == -1) {
			set.add(new Position(1, 0, 1));set.add(new Position(0, 0, 1));set.add(new Position(-1, 0, 1));
			set.add(new Position(1, 1, 1));set.add(new Position(0, 1, 1));set.add(new Position(-1, 1, 1));
			set.add(new Position(1, -1, 1));set.add(new Position(0, -1, 1));set.add(new Position(-1, -1, 1));
		}
		return set;
	}

	public void startBreak(Minecraft minecraft) {
		if(debugmode) System.out.println("startBreak start");

		Set<Position> backDirections = getBackDirections(minecraft);
		positions.clear();
		vectors.clear();

		switch(mode) {
		case mode_off:
			break;
		case mode_line:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(getDirection(minecraft));
			break;
		case mode_tunnel:
			positions.add(new Position(prev_i, prev_j - 1, prev_k));
			positions.add(new Position(prev_i, prev_j, prev_k));
			positions.add(new Position(prev_i, prev_j + 1, prev_k));
			vectors.add(getDirection(minecraft));
			break;
		case mode_front_upper:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(new Position(1, 1, 1));vectors.add(new Position(1, 1, 0));vectors.add(new Position(1, 1, -1));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));

			vectors.add(new Position(0, 1, 1));vectors.add(new Position(0, 1, 0));vectors.add(new Position(0, 1, -1));
			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));

			vectors.add(new Position(-1, 1, 1));vectors.add(new Position(-1, 1, 0));vectors.add(new Position(-1, 1, -1));
			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			vectors.removeAll(backDirections);
			break;
		case mode_front_under:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));
			vectors.add(new Position(1, -1, 1));vectors.add(new Position(1, -1, 0));vectors.add(new Position(1, -1, -1));

			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));
			vectors.add(new Position(0, -1, 1));vectors.add(new Position(0, -1, 0));vectors.add(new Position(-1, -1, -1));

			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			vectors.add(new Position(-1, -1, 1));vectors.add(new Position(-1, -1, 0));vectors.add(new Position(-1, -1, -1));
			vectors.removeAll(backDirections);
			break;
		case mode_front:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(new Position(1, 1, 1));vectors.add(new Position(1, 1, 0));vectors.add(new Position(1, 1, -1));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));
			vectors.add(new Position(1, -1, 1));vectors.add(new Position(1, -1, 0));vectors.add(new Position(1, -1, -1));

			vectors.add(new Position(0, 1, 1));vectors.add(new Position(0, 1, 0));vectors.add(new Position(0, 1, -1));
			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));
			vectors.add(new Position(0, -1, 1));vectors.add(new Position(0, -1, 0));vectors.add(new Position(-1, -1, -1));

			vectors.add(new Position(-1, 1, 1));vectors.add(new Position(-1, 1, 0));vectors.add(new Position(-1, 1, -1));
			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			vectors.add(new Position(-1, -1, 1));vectors.add(new Position(-1, -1, 0));vectors.add(new Position(-1, -1, -1));
			vectors.removeAll(backDirections);
			break;
		case mode_upper:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(new Position(1, 1, 1));vectors.add(new Position(1, 1, 0));vectors.add(new Position(1, 1, -1));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));

			vectors.add(new Position(0, 1, 1));vectors.add(new Position(0, 1, 0));vectors.add(new Position(0, 1, -1));
			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));

			vectors.add(new Position(-1, 1, 1));vectors.add(new Position(-1, 1, 0));vectors.add(new Position(-1, 1, -1));
			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			break;
		case mode_under:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));
			vectors.add(new Position(0, -1, 1));vectors.add(new Position(0, -1, 0));vectors.add(new Position(-1, -1, -1));

			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			vectors.add(new Position(-1, -1, 1));vectors.add(new Position(-1, -1, 0));vectors.add(new Position(-1, -1, -1));

			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			vectors.add(new Position(-1, -1, 1));vectors.add(new Position(-1, -1, 0));vectors.add(new Position(-1, -1, -1));
			break;
		case mode_horizontal:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(new Position(1, 0, 0));vectors.add(new Position(-1, 0, 0));
			vectors.add(new Position(0, 0, 1));vectors.add(new Position(0, 0, -1));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, -1));
			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, -1));
			break;
		case mode_all:
			positions.add(new Position(prev_i, prev_j, prev_k));
			vectors.add(new Position(1, 1, 1));vectors.add(new Position(1, 1, 0));vectors.add(new Position(1, 1, -1));
			vectors.add(new Position(1, 0, 1));vectors.add(new Position(1, 0, 0));vectors.add(new Position(1, 0, -1));
			vectors.add(new Position(1, -1, 1));vectors.add(new Position(1, -1, 0));vectors.add(new Position(1, -1, -1));

			vectors.add(new Position(0, 1, 1));vectors.add(new Position(0, 1, 0));vectors.add(new Position(0, 1, -1));
			vectors.add(new Position(0, 0, 1));/*vectors.add(new Position(0, 0, 0));*/vectors.add(new Position(0, 0, -1));
			vectors.add(new Position(0, -1, 1));vectors.add(new Position(0, -1, 0));vectors.add(new Position(-1, -1, -1));

			vectors.add(new Position(-1, 1, 1));vectors.add(new Position(-1, 1, 0));vectors.add(new Position(-1, 1, -1));
			vectors.add(new Position(-1, 0, 1));vectors.add(new Position(-1, 0, 0));vectors.add(new Position(-1, 0, -1));
			vectors.add(new Position(-1, -1, 1));vectors.add(new Position(-1, -1, 0));vectors.add(new Position(-1, -1, -1));
			break;
		}
		if(debugmode) {
			System.out.print("positions ");
			System.out.println(positions);
			System.out.print("vectors ");
			System.out.println(vectors);
			System.out.print(minecraft.theWorld.getBlockId(prev_i+1, prev_j, prev_k));
			System.out.print(", ");
			System.out.print(minecraft.theWorld.getBlockId(prev_i-1, prev_j, prev_k));
			System.out.print(", ");
			System.out.print(minecraft.theWorld.getBlockId(prev_i, prev_j, prev_k+1));
			System.out.print(", ");
			System.out.print(minecraft.theWorld.getBlockId(prev_i, prev_j, prev_k-1));
			System.out.println();
		}

		if(debugmode) System.out.println("startBreak end");
	}

	public Set<Position> addNextBreakBlocks(Minecraft minecraft, Position position) {
		Set<Position> newPositions = new LinkedHashSet();
		for(Position vector : vectors) {
			Position pos = position.addVector(vector.x, vector.y, vector.z);
			int id = minecraft.theWorld.getBlockId((int)pos.x, (int)pos.y, (int)pos.z);
			if(id == blockId) {
				if(positions.contains(pos) == false && newPositions.contains(pos) == false) {
					if(debugmode) {
						System.out.print("addNextBreakBlocks ");
						System.out.println(pos.toString());
					}
					newPositions.add(pos);
				}
			}
		}

		return newPositions;
	}

}
