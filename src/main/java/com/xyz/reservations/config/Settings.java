package com.xyz.reservations.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public final class Settings {

    private static Config config = ConfigFactory.load();

    public static final int seatCount = config.getInt("reservation.seatcount");
    public static final int rowCount = config.getInt("reservation.rowcount");
    public static final int holdTimeout = config.getInt("reservation.holdtimeout");
}
