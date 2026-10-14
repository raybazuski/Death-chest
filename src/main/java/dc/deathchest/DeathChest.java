package dc.deathchest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class DeathChest extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent e) {
        List<ItemStack> drops = e.getDrops();
        int slots = drops.size();
        if (slots == 0) return;

        Player p = e.getEntity();
        World w = p.getWorld();
        int x = p.getLocation().getBlockX();
        int y = p.getLocation().getBlockY();
        int z = p.getLocation().getBlockZ();
        if (y < w.getMinHeight()) return;

        boolean dbl = slots > 27;
        int wx = x, ex = x;
        boolean found = false;

        for (int t = 0; t < 8 && y < w.getMaxHeight() - 1; t++, y++) {
            if (!w.getBlockAt(x, y, z).isReplaceable()) continue;
            if (!dbl) { found = true; break; }
            if (w.getBlockAt(x + 1, y, z).isReplaceable()) { wx = x; ex = x + 1; found = true; break; }
            if (w.getBlockAt(x - 1, y, z).isReplaceable()) { wx = x - 1; ex = x; found = true; break; }
        }
        if (!found) return;

        Block west = w.getBlockAt(wx, y, z);
        west.setBlockData(data(dbl ? Type.RIGHT : Type.SINGLE), false);
        if (dbl) w.getBlockAt(ex, y, z).setBlockData(data(Type.LEFT), false);

        // sign on the front (north side) with the player's name
        Block sb = w.getBlockAt(wx, y, z - 1);
        if (sb.isReplaceable()) {
            WallSign sd = (WallSign) Bukkit.createBlockData(Material.OAK_WALL_SIGN);
            sd.setFacing(BlockFace.NORTH);
            sb.setBlockData(sd, false);
            Sign s = (Sign) sb.getState();
            s.getSide(Side.FRONT).setLine(0, "RIP");
            s.getSide(Side.FRONT).setLine(1, p.getName());
            s.update(false, false);
        }

        Inventory inv = ((org.bukkit.block.Chest) west.getState()).getInventory();
        ItemStack[] items = drops.toArray(new ItemStack[0]);
        drops.clear();
        drops.addAll(inv.addItem(items).values());

        p.sendMessage("Your items are in a chest at " + wx + " " + y + " " + z);
    }

    private static org.bukkit.block.data.type.Chest data(Type type) {
        org.bukkit.block.data.type.Chest d =
                (org.bukkit.block.data.type.Chest) Bukkit.createBlockData(Material.CHEST);
        d.setFacing(BlockFace.NORTH);
        d.setType(type);
        return d;
    }
}
