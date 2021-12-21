package nl.mtvehicles.core.movement;

import net.minecraft.network.protocol.game.PacketPlayInSteerVehicle;
import net.minecraft.world.entity.Entity;
import nl.mtvehicles.core.Main;
import nl.mtvehicles.core.infrastructure.helpers.BossBarUtils;
import nl.mtvehicles.core.infrastructure.helpers.VehicleData;
import nl.mtvehicles.core.infrastructure.modules.ConfigModule;
import org.bukkit.*;
import org.bukkit.block.data.type.Fence;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.lang.reflect.Method;
import java.math.BigDecimal;

public class VehicleMovement1_18 {
    public static void vehicleMovement(Player p, PacketPlayInSteerVehicle ppisv) {
        long lastUsed = 0L;
        if (p.getVehicle() == null) return;

        if (!p.getVehicle().getType().toString().contains("ARMOR_STAND")) return;

        if (p.getVehicle().getCustomName() == null) return;

        if (p.getVehicle().getCustomName().replace("MTVEHICLES_MAINSEAT_", "") == null) return;

        String license = p.getVehicle().getCustomName().replace("MTVEHICLES_MAINSEAT_", "");

        if (VehicleData.autostand.get("MTVEHICLES_MAIN_" + license) == null) return;

        if (VehicleData.speed.get(license) == null) {
            VehicleData.speed.put(license, 0.0);
            return;
        }
        if (VehicleData.fuel.get(license) < 1) {
            BossBarUtils.setBossBarValue(0 / 100.0D, license);
            return;
        }

        BossBarUtils.setBossBarValue(VehicleData.fuel.get(license) / 100.0D, license);
        ArmorStand standMain = VehicleData.autostand.get("MTVEHICLES_MAIN_" + license);
        ArmorStand standSkin = VehicleData.autostand.get("MTVEHICLES_SKIN_" + license);
        ArmorStand standMainSeat = VehicleData.autostand.get("MTVEHICLES_MAINSEAT_" + license);
        ArmorStand standRotors = VehicleData.autostand.get("MTVEHICLES_WIEKENS_" + license);
        Bukkit.getScheduler().runTask(Main.instance, () -> {
            standSkin.teleport(new Location(standMain.getLocation().getWorld(), standMain.getLocation().getX(), standMain.getLocation().getY(), standMain.getLocation().getZ(), standSkin.getLocation().getYaw(), standSkin.getLocation().getPitch()));
        });
        int RotationSpeed = VehicleData.RotationSpeed.get(license);
        double MaxSpeed = VehicleData.MaxSpeed.get(license);
        double AccelerationSpeed = VehicleData.AccelerationSpeed.get(license);
        double BrakingSpeed = VehicleData.BrakingSpeed.get(license);
        double MaxSpeedBackwards = VehicleData.MaxSpeedBackwards.get(license);
        double FrictionSpeed = VehicleData.FrictionSpeed.get(license);

        updateStand(standMain, license, steerIsJumping(ppisv));
        slabCheck(standMain, license);
        mainSeat(standMain, standMainSeat, license);

        if (VehicleData.seatsize.get(license + "addon") != null) {
            for (int i = 1; i <= VehicleData.seatsize.get(license + "addon"); i++) {
                ArmorStand standAddon = VehicleData.autostand.get("MTVEHICLES_ADDON" + i + "_" + license);
                Bukkit.getScheduler().runTask(Main.instance, () -> {
                    standAddon.teleport(standMain.getLocation());
                });
            }
        }
        if (VehicleData.type.get(license) != null) {
            if (VehicleData.type.get(license).contains("HELICOPTER")) {
                rotors(standMain, standRotors, license);
            }
            if (VehicleData.type.get(license).contains("TANK")) {
                if (steerIsJumping(ppisv)) {
                    if (VehicleData.lastUsage.containsKey(p.getName())) {
                        lastUsed = ((Long) VehicleData.lastUsage.get(p.getName())).longValue();
                    }
                    if (System.currentTimeMillis() - lastUsed >= ConfigModule.defaultConfig.getConfig().getInt("hornCooldown") * 1000) {
                        standMain.getWorld().playEffect(standMain.getLocation(), Effect.BLAZE_SHOOT, 1, 1);
                        standMain.getWorld().playEffect(standMain.getLocation(), Effect.GHAST_SHOOT, 1, 1);
                        standMain.getWorld().playEffect(standMain.getLocation(), Effect.WITHER_BREAK_BLOCK, 1, 1);
                        double xOffset = 4;
                        double yOffset = 1.6;
                        double zOffset = 0;
                        Location locvp = standMain.getLocation().clone();
                        Location fbvp = locvp.add(locvp.getDirection().setY(0).normalize().multiply(xOffset));
                        float zvp = (float) (fbvp.getZ() + zOffset * Math.sin(Math.toRadians(fbvp.getYaw())));
                        float xvp = (float) (fbvp.getX() + zOffset * Math.cos(Math.toRadians(fbvp.getYaw())));
                        Location loc = new Location(standMain.getWorld(), xvp, standMain.getLocation().getY() + yOffset, zvp, fbvp.getYaw(), fbvp.getPitch());
                        standMain.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 2);
                        standMain.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 2);
                        standMain.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 5);
                        standMain.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, loc, 5);
                        VehicleData.lastUsage.put(p.getName(), Long.valueOf(System.currentTimeMillis()));
                    }
                }
            }
            if (!VehicleData.type.get(license).contains("HELICOPTER")) {
                if (!VehicleData.type.get(license).contains("TANK")) {
                    if (steerIsJumping(ppisv)) {
                        if (VehicleData.lastUsage.containsKey(p.getName())) {
                            lastUsed = ((Long) VehicleData.lastUsage.get(p.getName())).longValue();
                        }
                        if (System.currentTimeMillis() - lastUsed >= ConfigModule.defaultConfig.getConfig().getInt("hornCooldown") * 1000) {
                            standMain.getWorld().playSound(standMain.getLocation(), ConfigModule.defaultConfig.getConfig().getString("hornType"), 0.9f, 1f);
                            VehicleData.lastUsage.put(p.getName(), Long.valueOf(System.currentTimeMillis()));
                        }
                    }
                }
            }
        }
        if (steerGetXxa(ppisv) > 0.0) {
            Bukkit.getScheduler().runTask(Main.instance, () -> {
                standMain.teleport(new Location(standMain.getLocation().getWorld(), standMain.getLocation().getX(), standMain.getLocation().getY(), standMain.getLocation().getZ(), standMain.getLocation().getYaw() - RotationSpeed, standMain.getLocation().getPitch()));
                standMainSeat.teleport(new Location(standMain.getLocation().getWorld(), standMain.getLocation().getX(), standMain.getLocation().getY(), standMain.getLocation().getZ(), standMain.getLocation().getYaw() - RotationSpeed, standMain.getLocation().getPitch()));
                standSkin.teleport(new Location(standSkin.getLocation().getWorld(), standSkin.getLocation().getX(), standSkin.getLocation().getY(), standSkin.getLocation().getZ(), standSkin.getLocation().getYaw() - RotationSpeed, standSkin.getLocation().getPitch()));
            });
        } else if (steerGetXxa(ppisv) < 0.0) {
            Bukkit.getScheduler().runTask(Main.instance, () -> {
                standSkin.teleport(new Location(standSkin.getLocation().getWorld(), standSkin.getLocation().getX(), standSkin.getLocation().getY(), standSkin.getLocation().getZ(), standSkin.getLocation().getYaw() + RotationSpeed, standSkin.getLocation().getPitch()));
                standMainSeat.teleport(new Location(standMain.getLocation().getWorld(), standMain.getLocation().getX(), standMain.getLocation().getY(), standMain.getLocation().getZ(), standMain.getLocation().getYaw() + RotationSpeed, standMain.getLocation().getPitch()));
                standMain.teleport(new Location(standMain.getLocation().getWorld(), standMain.getLocation().getX(), standMain.getLocation().getY(), standMain.getLocation().getZ(), standMain.getLocation().getYaw() + RotationSpeed, standMain.getLocation().getPitch()));
            });
        }
        if (steerGetZza(ppisv) > 0.0) {
            if (VehicleData.speed.get(license) < 0) {
                VehicleData.speed.put(license, VehicleData.speed.get(license) + BrakingSpeed);
                return;
            }
            if (ConfigModule.defaultConfig.getConfig().getBoolean("benzine") && ConfigModule.vehicleDataConfig.getConfig().getBoolean("vehicle." + license + ".benzineEnabled")) {
                double dnum = VehicleData.fuel.get(license) - VehicleData.fuelUsage.get(license);
                VehicleData.fuel.put(license, dnum);
            }
            if (VehicleData.speed.get(license) > MaxSpeed-AccelerationSpeed) {
                return;
            }
            VehicleData.speed.put(license, VehicleData.speed.get(license) + AccelerationSpeed);
        }
        if (steerGetZza(ppisv) < 0.0) {
            if (VehicleData.speed.get(license) > 0) {
                VehicleData.speed.put(license, VehicleData.speed.get(license) - BrakingSpeed);
                return;
            }
            if (ConfigModule.defaultConfig.getConfig().getBoolean("benzine") && ConfigModule.vehicleDataConfig.getConfig().getBoolean("vehicle." + license + ".benzineEnabled")) {
                double dnum = VehicleData.fuel.get(license) - VehicleData.fuelUsage.get(license);
                VehicleData.fuel.put(license, dnum);
            }
            if (VehicleData.speed.get(license) < -MaxSpeedBackwards) {
                return;
            }
            VehicleData.speed.put(license, VehicleData.speed.get(license) - AccelerationSpeed);
        }
        if (steerGetZza(ppisv) == 0.0) {
            BigDecimal round = BigDecimal.valueOf(VehicleData.speed.get(license)).setScale(1, BigDecimal.ROUND_DOWN);
            if (Double.parseDouble(String.valueOf(round)) == 0.0) {
                VehicleData.speed.put(license, 0.0);
                return;
            }
            if (Double.parseDouble(String.valueOf(round)) > 0.01) {
                VehicleData.speed.put(license, VehicleData.speed.get(license) - FrictionSpeed);
                return;
            }
            if (Double.parseDouble(String.valueOf(round)) < 0.01) {
                VehicleData.speed.put(license, VehicleData.speed.get(license) + FrictionSpeed);
            }
        }
    }

    public static void slabCheck(ArmorStand mainStand, String license) {
        double xOffset = 0.7;
        double yOffset = 0.4;
        double zOffset = 0.0;
        Location locvp = mainStand.getLocation().clone();
        Location fbvp = locvp.add(locvp.getDirection().setY(0).normalize().multiply(xOffset));
        float zvp = (float) (fbvp.getZ() + zOffset * Math.sin(Math.toRadians(fbvp.getYaw())));
        float xvp = (float) (fbvp.getX() + zOffset * Math.cos(Math.toRadians(fbvp.getYaw())));
        Location loc = new Location(mainStand.getWorld(), xvp, mainStand.getLocation().getY() + yOffset, zvp, fbvp.getYaw(), fbvp.getPitch());
        String locY = String.valueOf(mainStand.getLocation().getY());
        Location locBlockAbove = new Location(mainStand.getWorld(), xvp, mainStand.getLocation().getY() + yOffset + 1, zvp, fbvp.getYaw(), fbvp.getPitch());;

        if (loc.getBlock().getType().toString().contains("CARPET") && ConfigModule.defaultConfig.getConfig().getBoolean("driveOnCarpets")){

            if (!locBlockAbove.getBlock().isPassable()) {
                VehicleData.speed.put(license, 0.0);
                return;
            }

            if (locY.substring(locY.length() - 2).contains(".0")) {
                pushVehicleUp(mainStand, 0.0625);
            }
            return;
        }

        if (driveUpSlabs()){
            if (locY.substring(locY.length() - 2).contains(".5")) {
                if (loc.getBlock().isPassable()) {
                    return;
                }
                if (loc.getBlock().getBlockData() instanceof Slab) {
                    Slab slab = (Slab) loc.getBlock().getBlockData();
                    if (slab.getType().toString().equals("BOTTOM")) {
                        return;
                    }
                }
                if (!locBlockAbove.getBlock().isPassable()) {
                    VehicleData.speed.put(license, 0.0);
                    return;
                }

                pushVehicleUp(mainStand, 0.5);
                return;
            }
            if (loc.getBlock().getBlockData() instanceof Slab){
                Slab slab = (Slab) loc.getBlock().getBlockData();
                if (slab.getType().toString().equals("BOTTOM")){
                    if (!locBlockAbove.getBlock().isPassable()) {
                        VehicleData.speed.put(license, 0.0);
                        return;
                    }
                    if (locY.substring(locY.length() - 2).contains(".0")) {
                        pushVehicleUp(mainStand, 0.5);
                    } else {
                        double difference = Double.parseDouble("0." + locY.split("\\.")[1]);
                        if ((0.5 - difference) > 0){
                            pushVehicleUp(mainStand, 0.5 - difference);
                        }
                    }
                } else {
                    VehicleData.speed.put(license, 0.0);
                    return;
                }
            } else {
                if (!loc.getBlock().isPassable()) {
                    VehicleData.speed.put(license, 0.0);
                    return;
                }
            }
        } else {

            if (loc.getBlock().getBlockData() instanceof Fence || loc.getBlock().getType().toString().contains("WALL") || loc.getBlock().getBlockData() instanceof TrapDoor){
                VehicleData.speed.put(license, 0.0);
                return;
            }

            if (!locY.substring(locY.length() - 2).contains(".5")) {
                if (!loc.getBlock().isPassable()) {
                    if (loc.getBlock().getBlockData() instanceof Slab){
                        Slab slab = (Slab) loc.getBlock().getBlockData();
                        if (slab.getType().toString().equals("BOTTOM")){
                            VehicleData.speed.put(license, 0.0);
                            return;
                        }
                    }

                    if (!locBlockAbove.getBlock().isPassable()) { //if more than 1 block high
                        VehicleData.speed.put(license, 0.0);
                        return;
                    }

                    if (locY.substring(locY.length() - 2).contains(".0")) {
                        pushVehicleUp(mainStand, 1);
                    } else {
                        double difference = Double.parseDouble("0." + locY.split("\\.")[1]);
                        if ((1 - difference) > 0){
                            pushVehicleUp(mainStand, 1 - difference);
                        }
                    }
                }
            }
            if (locY.substring(locY.length() - 2).contains(".5")) { //Only if a vehicle is placed on a slab
                if (loc.getBlock().isPassable()) {
                    return;
                }
                if (loc.getBlock().getBlockData() instanceof Slab){
                    Slab slab = (Slab) loc.getBlock().getBlockData();
                    if (slab.getType().toString().equals("BOTTOM")){
                        return;
                    }
                }

                if (!locBlockAbove.getBlock().isPassable()) {
                    VehicleData.speed.put(license, 0.0);
                    return;
                }

                pushVehicleUp(mainStand, 0.5);
                return;
            }
        }
    }

    public static void updateStand(ArmorStand mainStand, String license, Boolean space) {
        Location loc = mainStand.getLocation();
        Location location = new Location(loc.getWorld(), loc.getX(), loc.getY() - 0.2, loc.getZ(), loc.getYaw(), loc.getPitch());

        if (VehicleData.type.get(license) == null) return;

        if (VehicleData.type.get(license).contains("HELICOPTER")) {
            if (!location.getBlock().getType().equals(Material.AIR)) {
                VehicleData.speed.put(license, 0.0);
            }
            if (space) {
                if (mainStand.getLocation().getY() > Main.instance.getConfig().getInt("helicopterMaxHeight")) {
                    return;
                }
                mainStand.setVelocity(new Vector(mainStand.getLocation().getDirection().multiply(VehicleData.speed.get(license)).getX(), 0.2, mainStand.getLocation().getDirection().multiply(VehicleData.speed.get(license)).getZ()));
                return;
            }
            mainStand.setVelocity(new Vector(mainStand.getLocation().getDirection().multiply(VehicleData.speed.get(license)).getX(), -0.2, mainStand.getLocation().getDirection().multiply(VehicleData.speed.get(license)).getZ()));
            return;
        }

        if (VehicleData.type.get(license).contains("HOVER")) {
            if (location.getBlock().getType().toString().contains("AIR")) {
                mainStand.setVelocity(new Vector(mainStand.getLocation().getDirection().multiply(VehicleData.speed.get(license)).getX(), -0.8, mainStand.getLocation().getDirection().multiply(VehicleData.speed.get(license)).getZ()));
                return;
            }
            mainStand.setVelocity(new Vector(mainStand.getLocation().getDirection().multiply(VehicleData.speed.get(license)).getX(), 0.00001, mainStand.getLocation().getDirection().multiply(VehicleData.speed.get(license)).getZ()));
            return;
        }

        if (location.getBlock().getType().toString().contains("AIR") || location.getBlock().getType().toString().contains("WATER")) {
            mainStand.setVelocity(new Vector(mainStand.getLocation().getDirection().multiply(VehicleData.speed.get(license)).getX(), -0.8, mainStand.getLocation().getDirection().multiply(VehicleData.speed.get(license)).getZ()));
            return;
        }
        if (location.getBlock().getType().toString().contains("CARPET")){
            mainStand.setVelocity(new Vector(mainStand.getLocation().getDirection().multiply(VehicleData.speed.get(license)).getX(), -0.7375, mainStand.getLocation().getDirection().multiply(VehicleData.speed.get(license)).getZ()));
            return;
        }
        mainStand.setVelocity(new Vector(mainStand.getLocation().getDirection().multiply(VehicleData.speed.get(license)).getX(), 0.0, mainStand.getLocation().getDirection().multiply(VehicleData.speed.get(license)).getZ()));
    }

    public static void mainSeat(ArmorStand mainStand, ArmorStand mainSeat, String license) {
        if (VehicleData.seatsize.get(license) != null) {
            for (int i = 2; i <= VehicleData.seatsize.get(license); i++) {
                ArmorStand seatas = VehicleData.autostand.get("MTVEHICLES_SEAT" + i + "_" + license);
                double xOffset = VehicleData.seatx.get("MTVEHICLES_SEAT" + i + "_" + license);
                double yOffset = VehicleData.seaty.get("MTVEHICLES_SEAT" + i + "_" + license);
                double zOffset = VehicleData.seatz.get("MTVEHICLES_SEAT" + i + "_" + license);
                Location locvp = mainStand.getLocation().clone();
                Location fbvp = locvp.add(locvp.getDirection().setY(0).normalize().multiply(xOffset));
                float zvp = (float) (fbvp.getZ() + zOffset * Math.sin(Math.toRadians(fbvp.getYaw())));
                float xvp = (float) (fbvp.getX() + zOffset * Math.cos(Math.toRadians(fbvp.getYaw())));
                Location loc = new Location(mainStand.getWorld(), xvp, mainStand.getLocation().getY() + yOffset, zvp, fbvp.getYaw(), fbvp.getPitch());
                Entity seat = ((CraftEntity) seatas).getHandle();
                Bukkit.getScheduler().runTask(Main.instance, () -> {
                    try {
                        Method method = seat.getClass().getSuperclass().getSuperclass().getDeclaredMethod("a", double.class, double.class, double.class, float.class, float.class); //teleporting an occupied vehicle
                        method.invoke(seat, loc.getX(), loc.getY(), loc.getZ(), fbvp.getYaw(), loc.getPitch());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
        double xOffset = VehicleData.mainx.get("MTVEHICLES_MAINSEAT_" + license);
        double yOffset = VehicleData.mainy.get("MTVEHICLES_MAINSEAT_" + license);
        double zOffset = VehicleData.mainz.get("MTVEHICLES_MAINSEAT_" + license);
        Location locvp = mainStand.getLocation().clone();
        Location fbvp = locvp.add(locvp.getDirection().setY(0).normalize().multiply(xOffset));
        float zvp = (float) (fbvp.getZ() + zOffset * Math.sin(Math.toRadians(fbvp.getYaw())));
        float xvp = (float) (fbvp.getX() + zOffset * Math.cos(Math.toRadians(fbvp.getYaw())));
        Location loc = new Location(mainStand.getWorld(), xvp, mainStand.getLocation().getY() + yOffset, zvp, fbvp.getYaw(), fbvp.getPitch());
        Entity seat = ((CraftEntity) mainSeat).getHandle();
        Bukkit.getScheduler().runTask(Main.instance, () -> {
            try {
                Method method = seat.getClass().getSuperclass().getSuperclass().getDeclaredMethod("a", double.class, double.class, double.class, float.class, float.class); //teleporting an occupied vehicle
                method.invoke(seat, loc.getX(), loc.getY(), loc.getZ(), fbvp.getYaw(), loc.getPitch());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void rotors(ArmorStand main, ArmorStand seatas, String license) {
        double xOffset = VehicleData.wiekenx.get("MTVEHICLES_WIEKENS_" + license);
        double yOffset = VehicleData.wiekeny.get("MTVEHICLES_WIEKENS_" + license);
        double zOffset = VehicleData.wiekenz.get("MTVEHICLES_WIEKENS_" + license);
        final Location locvp = main.getLocation().clone();
        final Location fbvp = locvp.add(locvp.getDirection().setY(0).normalize().multiply(xOffset));
        final float zvp = (float) (fbvp.getZ() + zOffset * Math.sin(Math.toRadians(seatas.getLocation().getYaw())));
        final float xvp = (float) (fbvp.getX() + zOffset * Math.cos(Math.toRadians(seatas.getLocation().getYaw())));
        final Location loc = new Location(main.getWorld(), xvp, main.getLocation().getY() + yOffset, zvp, seatas.getLocation().getYaw() + 15, seatas.getLocation().getPitch());
        Bukkit.getScheduler().runTask(Main.instance, () -> {
            seatas.teleport(loc);
        });
    }

    private static boolean driveUpSlabs(){
        if (ConfigModule.defaultConfig.getConfig().getString("driveUp").equals("blocks")){
            return false;
        }
        return true;
    }

    private static void pushVehicleUp(ArmorStand mainStand, double plus){
        Location newLoc = new Location(mainStand.getLocation().getWorld(), mainStand.getLocation().getX(), mainStand.getLocation().getY() + plus, mainStand.getLocation().getZ(), mainStand.getLocation().getYaw(), mainStand.getLocation().getPitch());
        Bukkit.getScheduler().runTask(Main.instance, () -> {
            mainStand.teleport(newLoc);
        });
    }

    private static void debugLog(String s){
        Bukkit.getConsoleSender().sendMessage(s);
    }

    private static boolean steerIsJumping(PacketPlayInSteerVehicle packet){
        boolean isJumping = false;
        try {
            Method method = packet.getClass().getDeclaredMethod("d");
            isJumping = (Boolean) method.invoke(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isJumping;
    }

    private static float steerGetXxa(PacketPlayInSteerVehicle packet){
        float Xxa = 0;
        try {
            Method method = packet.getClass().getDeclaredMethod("b");
            Xxa = (float) method.invoke(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Xxa;
    }

    private static float steerGetZza(PacketPlayInSteerVehicle packet){
        float Zza = 0;
        try {
            Method method = packet.getClass().getDeclaredMethod("c");
            Zza = (float) method.invoke(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Zza;
    }
}