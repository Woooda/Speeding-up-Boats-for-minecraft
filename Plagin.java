import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Boat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

public class BoatSpeedPlugin extends JavaPlugin implements Listener {

    private int countdownTicks; // Количество тиков до следующего ускорения
    private int countdownTimer; // Текущий счетчик времени

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        // Загружаем настройки из конфигурационного файла
        loadConfig();

        // Запускаем таймер для отсчета времени до следующего ускорения
        new BukkitRunnable() {
            @Override
            public void run() {
                countdownTimer--;
                if (countdownTimer <= 0) {
                    // Сбрасываем таймер и ускоряем лодки
                    countdownTimer = countdownTicks;
                    accelerateBoats();
                }
                updateActionBar();
            }
        }.runTaskTimer(this, 20L, 20L); // Запускаем каждую секунду
    }

    @EventHandler
    public void onVehicleCreate(VehicleCreateEvent event) {
        if (event.getVehicle() instanceof Boat) {
            // Устанавливаем начальную скорость лодки
            Boat boat = (Boat) event.getVehicle();
            boat.setMaxSpeed(0.4);
        }
    }

    @EventHandler
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        if (event.getVehicle() instanceof Boat) {
            // Убираем лодку из отслеживания при ее уничтожении
            // Это важно для оптимизации и предотвращения утечек памяти
            Boat boat = (Boat) event.getVehicle();
            boat.remove();
        }
    }

    private void accelerateBoats() {
        for (World world : getServer().getWorlds()) {
            world.getEntitiesByClass(Boat.class).forEach(boat -> {
                Location loc = boat.getLocation();
                if (loc.getBlock().getType() == Material.WATER) {
                    // Устанавливаем скорость лодки на 0.4 для более быстрого движения
                    boat.setVelocity(boat.getVelocity().multiply(1.5));
                }
            });
        }
    }

    private void updateActionBar() {
        getServer().getOnlinePlayers().forEach(player -> {
            // Обновляем ActionBar с отображением таймера
            player.sendActionBar("Следующее ускорение через: " + countdownTimer / 20 + " секунд");
        });
    }

    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            // Создаем конфигурационный файл с настройками по умолчанию, если его нет
            saveDefaultConfig();
        }

        // Загружаем настройки из конфигурационного файла
        FileConfiguration config = getConfig();
        countdownTicks = config.getInt("intervalSeconds", 10) * 20;
        countdownTimer = countdownTicks;
    }
}
