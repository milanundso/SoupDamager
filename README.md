#  Soup Damager for Minecraft 1.8.8

A lightweight **Spigot plugin** designed for **PvP soup training** in Minecraft **1.8.8**.  

---

##  Overview

The **Soup Damager** plugin is a training tool for PvP players who want to get better at **hotkeying and souping under pressure**.  
When a player enters specific **Soup Zones**, they’ll periodically take damage, forcing them to **soup efficiently** to survive.

Each zone has a different **difficulty level** and **damage intensity**, allowing for a progressive training experience.

---

## Features

- - Multiple **Soup Zones** with varying difficulty  
- - **Hardcoded Zone Positions** for consistency  
- - **Holograms** above each damager for clear identification  
- - **Randomized Crap Damager** item drops  

---

##  Soup Zones

The plugin defines several **hardcoded soup zones** in the **main class**:

```java
// Initialization of all soup zones
org.bukkit.World world = Bukkit.getWorlds().get(0);
soupZones.add(new SoupZone(new Location(world, -9993, 41, 109946), 5.0));
soupZones.add(new SoupZone(new Location(world, -10002, 41, 109945), 7.0));
soupZones.add(new SoupZone(new Location(world, -10011, 41, 109946), 10.0));
soupZones.add(new SoupZone(new Location(world, -10016, 41, 109953), 6.0));
```
