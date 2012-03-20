package net.minecraft.src;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.server.MinecraftServer;

public class mod_StoneBreakerMP extends BaseModMp {
	@MLProp
	public static String mode_line = "ON";
	@MLProp
	public static String mode_tunnel = "ON";
	@MLProp
	public static String mode_front_upper = "ON";
	@MLProp
	public static String mode_front_under = "ON";
	@MLProp
	public static String mode_front = "ON";
	@MLProp
	public static String mode_upper = "ON";
	@MLProp
	public static String mode_under = "ON";
	@MLProp
	public static String mode_horizontal = "ON";
	@MLProp
	public static String mode_all = "ON";

	public static boolean mode[] = new boolean[10];

	public static final int cmd_break = 0;
	public static final int cmd_mode = 1;
	public static final int cmd_target = 2;
	public static final int cmd_limit = 3;

	@MLProp(info = "separate by ','")
	public static String blockIDs = "1";

	public static Set<Integer> targetIDs = new HashSet();

	@MLProp(info = "maximum number of block break (0 = unlimited)")
	public static int breaklimit = 0;

	class BreakResister {
		public EntityPlayerMP player;
		int i;
		int j;
		int k;
		int blockId;
		int metadata;
		int stacksize;
		int itemdamage;
		World worldObj;

		public BreakResister(EntityPlayerMP entityplayermp, int i, int j, int k, int blockId, int metadata) {
			this.player = entityplayermp;
			this.i = i;
			this.j = j;
			this.k = k;
			this.blockId = blockId;
			this.metadata = metadata;
			this.worldObj = entityplayermp.worldObj;
		}
	}

	static List<BreakResister> breakResisters = new ArrayList();

	@Override
	public String getVersion() {
		return "[1.2.3] StoneBreakerMP 0.0.1";
	}

	@Override
	public void load() {

		mode[1] = mode_line == "ON";
		mode[2] = mode_tunnel == "ON";
		mode[3] = mode_front_upper == "ON";
		mode[4] = mode_front_under == "ON";
		mode[5] = mode_front == "ON";
		mode[6] = mode_upper == "ON";
		mode[7] = mode_under == "ON";
		mode[8] = mode_horizontal == "ON";
		mode[9] = mode_all == "ON";

		String strMode = "StoneBreak mode = [";
		if(mode[0]) strMode += "OFF";
		if(mode[1]) strMode += " LINE";
		if(mode[2]) strMode += " TUNNEL";
		if(mode[3]) strMode += " FRONT_UPPER";
		if(mode[4]) strMode += " FRONT_UNDER";
		if(mode[5]) strMode += " FRONT";
		if(mode[6]) strMode += " UPPER";
		if(mode[7]) strMode += " UNDER";
		if(mode[8]) strMode += " HORIZONTAL";
		if(mode[9]) strMode += " ALL";
		strMode += "]";
		System.out.println(strMode);

		String str = blockIDs;
		String[] tokens = str.split(",");
		for(String token : tokens) {
			targetIDs.add(Integer.parseInt(token.trim()));
		}

		String s = "StoneBreaker target = ";
		s += targetIDs;

		System.out.println(s);

		ModLoader.setInGameHook(this, true, true);
	}

	public void registerBreak(EntityPlayerMP entityplayermp, int i, int j, int k, int blockId, int metadata) {
		breakResisters.add(new BreakResister(entityplayermp, i, j, k, blockId, metadata));
	}

	@Override
	public void handlePacket(Packet230ModLoader packet230modloader,
			EntityPlayerMP entityplayermp) {

		System.out.printf("[%d] recv %d, %d, %d, %d, %d, %d\n",
				packet230modloader.modId, packet230modloader.dataInt[0],
				packet230modloader.dataInt[1], packet230modloader.dataInt[2], packet230modloader.dataInt[3],
				packet230modloader.dataInt[4], packet230modloader.dataInt[5]);

		switch(packet230modloader.dataInt[0]) {
		case cmd_break:
			registerBreak(entityplayermp, packet230modloader.dataInt[1], packet230modloader.dataInt[2],
					packet230modloader.dataInt[3], packet230modloader.dataInt[4], packet230modloader.dataInt[5]);
			break;
		}
		super.handlePacket(packet230modloader, entityplayermp);
	}

	@Override
	public void onTickInGame(MinecraftServer minecraftserver) {
		for(BreakResister breakResister : breakResisters) {
			breakBlock(minecraftserver, breakResister);
		}
		breakResisters.clear();
		return;
	}

	public void breakBlock(MinecraftServer minecraftserver,
			BreakResister breakResister) {
		System.out.println("breakBlock");

		int blockId = breakResister.worldObj.getBlockId(breakResister.i, breakResister.j, breakResister.k);
		if(Block.blocksList[blockId] == null) {
			return;
		}

		Material material = breakResister.worldObj.getBlockMaterial(breakResister.i, breakResister.j, breakResister.k);
		if(material.isSolid() == false) {
			return;
		}

		System.out.printf("breakBlock %d, %d, %d\n", breakResister.i, breakResister.j, breakResister.k);

        if (breakResister.worldObj.getBlockId(breakResister.i, breakResister.j, breakResister.k) != 0)
        {
    		breakResister.player.itemInWorldManager.blockHarvessted(breakResister.i, breakResister.j, breakResister.k);
        	breakResister.player.playerNetServerHandler.sendPacket(new Packet53BlockChange(breakResister.i, breakResister.j, breakResister.k, breakResister.worldObj));
        }
		/*
        int i_ = breakResister.worldObj.getBlockMetadata(breakResister.i, breakResister.j, breakResister.k);
		int id = breakResister.worldObj.getBlockId(breakResister.i, breakResister.j, breakResister.k);
		Block block_ = Block.blocksList[id];
	    boolean flag = breakResister.worldObj.setBlockWithNotify(breakResister.i, breakResister.j, breakResister.k, id);
		if(block_ != null) {
	       	block_.onBlockDestroyedByPlayer(breakResister.worldObj,breakResister.i, breakResister.j, breakResister.k, i_);

			ItemStack itemstack = breakResister.player.getCurrentEquippedItem();
	        boolean flag1 = breakResister.player.canHarvestBlock(block_);

	        boolean ret = true;
	        if (itemstack != null)
	        {
	            itemstack.onDestroyBlock(id, breakResister.i, breakResister.j, breakResister.k, breakResister.player);

	            if (itemstack.stackSize == 0)
	            {
	                itemstack.onItemDestroyedByUse(breakResister.player);
	                breakResister.player.destroyCurrentEquippedItem();
	                ret = false;
	            }
	        }

	        if (flag && flag1)
	        {
	        	block_.harvestBlock(breakResister.worldObj, breakResister.player, breakResister.i, breakResister.j, breakResister.k, i_);
	        }
		}*/
	}

	@Override
	public void handleLogin(EntityPlayerMP entityplayermp) {
		sendBreakMode(entityplayermp);
		sendTargetIds(entityplayermp);
		sendBreakLimit(entityplayermp);
	}

	public void sendBreakLimit(EntityPlayerMP entityplayermp) {
		Packet230ModLoader packet = new Packet230ModLoader();
		packet.modId = getId();
		packet.dataInt = new int[2];
		packet.dataInt[0] = cmd_limit;
		packet.dataInt[1] = breaklimit;
		ModLoaderMp.sendPacketTo(this, entityplayermp, packet);
	}

	public void sendBreakMode(EntityPlayerMP entityplayermp) {
		Packet230ModLoader packet = new Packet230ModLoader();
		packet.modId = getId();
		packet.dataInt = new int[mode.length + 1];
		packet.dataInt[0] = cmd_mode;
		for(int i = 1; i < mode.length; i++) {
			packet.dataInt[i] = (mode[i]) ? 1 : 0;
		}
		ModLoaderMp.sendPacketTo(this, entityplayermp, packet);
	}

	public void sendTargetIds(EntityPlayerMP entityplayermp) {
		Packet230ModLoader packet = new Packet230ModLoader();
		packet.modId = getId();
		packet.dataInt = new int[1];
		packet.dataInt[0] = cmd_target;
		packet.dataString = new String[1];
		packet.dataString[0] = blockIDs;
		ModLoaderMp.sendPacketTo(this, entityplayermp, packet);
	}

}
