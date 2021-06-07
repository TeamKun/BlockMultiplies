package net.kunmc.lab.blockmultiplies;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class Blockmultiplies extends JavaPlugin {

    public static Blockmultiplies plugin;
    HashMap<UUID, Boolean> playerDigging = new HashMap<>();

    @Override
    public void onEnable() {

        // Plugin startup logic
        System.out.println("プラグインが有効になりました");
        plugin = this;
        Config.loadConfig();

        Random rnd = new Random();

        // ProtocolLibraryでパケットを取得する。
        // Client.BLOCK_DIGパケットで、破壊開始（START_DESTROY_BLOCK）を検出し、MapにUUIDとBoolean.TRUEを保存。
        // Client.BLOCK_DIGパケットで、破壊開始以外を検出し、MapからUUIDのキーを削除。
        // Client.ARM_ANIMATIONパケットで、腕動作を検出する。UUIDでMapを検索し、Boolean.TRUEであれば、ブロック生成ロジックを起動する。

        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(this, PacketType.Play.Client.BLOCK_DIG) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                String digName = event.getPacket().getPlayerDigTypes().getValues().get(0).name();
                if (digName.equals("START_DESTROY_BLOCK")) {
                    playerDigging.put(event.getPlayer().getUniqueId(), Boolean.TRUE);
                } else {
                    playerDigging.remove(event.getPlayer().getUniqueId());
                }
            }
        });

        //
        protocolManager.addPacketListener(new PacketAdapter(this, PacketType.Play.Client.ARM_ANIMATION) {
            @Override
            public void onPacketReceiving(PacketEvent event) {

                // ブロックがエアー以外、および、そのユーザーのUUIDに破壊開始フラグが立っているかを確認。
                Material blockType = event.getPlayer().getTargetBlock(null, 4).getType();
                if ((blockType != Material.AIR) && (playerDigging.getOrDefault(event.getPlayer().getUniqueId(), Boolean.FALSE))) {

                    // 乱数を生成し、生成確率に応じてその後の処理を実行するかを決定。
                    if (rnd.nextInt(99) + 1 <= Config.possibility()) {

                        // プレイヤーの位置に、xyz各軸に対し、絶対値が生成範囲内の正負の乱数を加算する。
                        Location loc = event.getPlayer().getLocation();
                        Integer range = Config.rndRange();
                        loc.add(rnd.nextInt(range * 2) - range, rnd.nextInt(range * 2) - range, rnd.nextInt(range * 2) - range);
                        new BukkitRunnable() {
                            public void run() {

                                // 対象位置に破壊中のブロックと同じブロックを設置する。
                                loc.getBlock().setType(blockType);
                            }
                        }.runTask(plugin);
                    }
                }
            }
        });
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        int n;

        if (args.length < 1) {
            sender.sendMessage("引数が足りません");
        } else {
            try {
                switch (args[0]) {
                    case "rndRange":
                        if (args.length > 2) {
                            sender.sendMessage("引数が多すぎます");
                            return false;
                        } else if (args.length < 2) {
                            sender.sendMessage("引数が足りません");
                            return false;
                        }
                        n = Integer.parseInt(args[1]);
                        Config.setRndRange(sender, n);
                        return true;
                    case "possibility":
                        if (args.length > 2) {
                            sender.sendMessage("引数が多すぎます");
                            return false;
                        } else if (args.length < 2) {
                            sender.sendMessage("引数が足りません");
                            return false;
                        }
                        n = Integer.parseInt(args[1]);
                        Config.setPossibility(sender, n);
                        return true;
                    case "config":
                        if (args.length > 1) {
                            sender.sendMessage("引数が多すぎます");
                            return false;
                        }
                        sender.sendMessage("生成範囲：" + Config.rndRange() + "　生成確率：" + Config.possibility());
                        return true;
                    default:
                        sender.sendMessage("第一引数が存在しない関数です");
                }
            } catch (NumberFormatException nfex) {
                sender.sendMessage("第二引数は整数にしてください");
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("bm")) return super.onTabComplete(sender, command, alias, args);
        if (args.length == 1) {
            if (args[0].length() == 0) {
                return Arrays.asList("rndRange", "possibility", "config");
            } else {
                if ("rndRange".startsWith(args[0])) {
                    return Collections.singletonList("rndRange");
                } else if ("possibility".startsWith(args[0])) {
                    return Collections.singletonList("possibility");
                } else if ("config".startsWith(args[0])) {
                    return Collections.singletonList("config");
                }
            }
        }
        if (args.length == 2) {
            if ((args[0].equals("rndRange")) || (args[0].equals("possibility"))) {
                return Collections.singletonList("<number>");
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("プラグインが無効になりました");
    }
}
