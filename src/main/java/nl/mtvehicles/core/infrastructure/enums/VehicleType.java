package nl.mtvehicles.core.infrastructure.enums;

import nl.mtvehicles.core.infrastructure.modules.DependencyModule;
import org.bukkit.Location;

import java.util.Locale;

/**
 * Type of vehicle
 */
public enum VehicleType {
    /**
     * Car includes cars, bicycles, as well as motorcycles
     */
    CAR,
    BOAT,
    AMPHIBIOUS,
    SUBMARINE,
    /**
     * Vehicles floating in the air (hovercraft and UFO)
     */
    HOVER,
    /**
     * Tanks - with the ability to shoot
     */
    TANK,
    /**
     * Helicopters - with the ability to fly, with blades
     */
    HELICOPTER,
    /**
     * Airplanes - with the ability to fly, their movement differs from helicopters
     */
    AIRPLANE;

    public String getName() {
        return this.toString().substring(0, 1).toUpperCase() + this.toString().substring(1).toLowerCase(Locale.ROOT);
    }

    public boolean isCar() {
        return this.equals(CAR);
    }

    public boolean isHover() {
        return this.equals(HOVER);
    }

    public boolean isAmphibious() {
        return this.equals(AMPHIBIOUS);
    }

    public boolean isBoat() {
        return this.equals(BOAT);
    }

    public boolean isSubmarine() {
        return this.equals(SUBMARINE);
    }

    public boolean isTank() {
        return this.equals(TANK);
    }

    public boolean isHelicopter() {
        return this.equals(HELICOPTER);
    }

    public boolean isAirplane() {
        return this.equals(AIRPLANE);
    }

    /**
     * Vehicle is considered as 'able to fly' if it is either an Airplane or a Helicopter
     *
     * @return True if vehicle can fly
     */
    public boolean canFly() {
        return this.equals(AIRPLANE) || this.equals(HELICOPTER);
    }

    /**
     * Check if usage of this vehicle type is disabled in a certain location (by WorldGuard flags)
     *
     * @param loc Location where the vehicle is being used (placed, clicked, ...)
     * @return True if the vehicle can't be used. (Returns false if WorldGuard is not enabled)
     */
    public boolean isUsageDisabled(Location loc) {
        if (!DependencyModule.isDependencyEnabled(SoftDependency.WORLD_GUARD)) return false;
        return DependencyModule.worldGuard.isInRegionWithFlag(loc, "mtv-use-" + this.toString().toLowerCase(Locale.ROOT), false);
    }
}
