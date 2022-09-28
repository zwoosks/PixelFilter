package me.zwoosks.pixelfilter.utils;

import com.mysql.cj.x.protobuf.MysqlxPrepare;
import me.zwoosks.pixelfilter.PixelFilter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.scheduler.BukkitRunnable;

import javax.imageio.ImageIO;
import javax.xml.crypto.Data;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.*;
import java.sql.*;
import java.util.Random;

public class DatabaseManager {

    private PixelFilter plugin;

    private String url;
    private String user;
    private String password;
    private String databaseName;

    public DatabaseManager(FileConfiguration config, PixelFilter plugin) {
        this.url = "jdbc:mysql://" + config.getString("database.address") + ":"
                + config.getString("database.port") + "/" + config.getString("database.db");
        this.user = config.getString("database.user");
        this.password = config.getString("database.password");
        this.databaseName = config.getString("database.db");
        this.plugin = plugin;
    }

    public void checkTables() {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            if(!tableExistsSQL(con, "allowedPixels")) createTable("allowedPixels", databaseName);
            if(!tableExistsSQL(con, "blacklistedPixels")) createTable("blacklistedPixels", databaseName);
            if(!tableExistsSQL(con, "pendingPixels")) createTable("pendingPixels", databaseName);
            if(!tableExistsSQL(con, "mapsToRemove")) createMapsToRemoveTable(databaseName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertToRemoveMap(String playerName, String uuid, int mapID) {
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            String query = "INSERT INTO `" + databaseName + "`.`mapsToRemove` (nick,uuid,map_id,date_left) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = con.prepareStatement(query);
            pstmt.setString(1, playerName.toLowerCase());
            pstmt.setString(2, uuid);
            pstmt.setInt(3, mapID);
            long millis = System.currentTimeMillis();
            Date date = new Date(millis);
            pstmt.setDate(4, date);
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void checkIfRemovableMaps(Player player) {
        try {
            String query = "SELECT * FROM `" + databaseName + "`.`mapsToRemove` where nick like '" + player.getName().toLowerCase() + "'";
            Connection con = DriverManager.getConnection(url, user, password);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while(rs.next()) {
                int mapID = rs.getInt("map_id");
                removeMap(player.getInventory(), mapID, player);
                // remove from database
                removeToRemoveMapRow(player.getName().toLowerCase());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void removeToRemoveMapRow(String playername) {
        try {
            String query = "DELETE FROM `" + databaseName + "`.`mapsToRemove` where nick = ?";
            Connection con = DriverManager.getConnection(url, user, password);
            PreparedStatement st = con.prepareStatement(query);
            st.setString(1, playername);
            st.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createMapsToRemoveTable(String database) {
        String query = "CREATE TABLE `" + database + "`.`mapsToRemove` (" +
                "  `id` INT NOT NULL AUTO_INCREMENT," +
                "  `nick` VARCHAR(45) NOT NULL," +
                "  `uuid` VARCHAR(120) NULL," +
                "  `map_id` INT NOT NULL," +
                "  `date_left` DATE NULL," +
                "  PRIMARY KEY (`id`));";
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement statement = con.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException e ) {
            System.out.println("An error has occured on Table Creation. Table name: mapsToRemove");
            e.printStackTrace();
        }
    }

    private void createTable(String name, String database) {
        String query = "CREATE TABLE `" + database + "`.`" + name + "` (" +
                "  `id` INT NOT NULL AUTO_INCREMENT," +
                "  `x` INT NOT NULL," +
                "  `z` INT NOT NULL," +
                "  `playerName` VARCHAR(45) NOT NULL," +
                "  `uuid` VARCHAR(120) NOT NULL," +
                "  `image` BLOB NOT NULL," +
                "  PRIMARY KEY (`id`));";
        try {
            Connection con = DriverManager.getConnection(url, user, password);
            Statement statement = con.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException e ) {
            System.out.println("An error has occured on Table Creation. Table name: " + name);
            e.printStackTrace();
        }
    }

    private boolean tableExistsSQL(Connection connection, String tableName) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT count(*) "
                + "FROM information_schema.tables "
                + "WHERE table_name = ?"
                + "LIMIT 1;");
        preparedStatement.setString(1, tableName);

        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1) != 0;
    }

    public boolean isMapOnDatabase(BufferedImage bufferedImage, String tableName, float minSimilarity) {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            Statement sta = connection.createStatement();
            ResultSet res = sta.executeQuery(
                    "SELECT * FROM " + databaseName + "." +  tableName);
            while(res.next()) {
                Blob databaseBlob = res.getBlob("image");
                InputStream in = databaseBlob.getBinaryStream();
                BufferedImage dbImage = ImageIO.read(in);

                // save dbImage on PC
                File outputfile = new File("C:\\Users\\iuri_\\Desktop\\ws\\pixelfilter\\plugins\\img.jpg");
                ImageIO.write(dbImage, "jpg", outputfile);

                // ------

                // save bufferedImage on PC
                File outputfile2 = new File("C:\\Users\\iuri_\\Desktop\\ws\\pixelfilter\\plugins\\img22.jpg");
                ImageIO.write(bufferedImage, "jpg", outputfile2);

                // here compare bufferedimage and given blob
                float similarity = compareImage(dbImage, bufferedImage);
                if(similarity >= minSimilarity) return true;
                else return false;
            }
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private float compareImage(BufferedImage biA, BufferedImage biB) {
        float percentage = 0;
        try {
            DataBuffer dbA = biA.getData().getDataBuffer();
            int sizeA = dbA.getSize();
            DataBuffer dbB = biB.getData().getDataBuffer();
            int sizeB = dbB.getSize();
            int count = 0;
            // compare data-buffer objects //
            if (sizeA == sizeB) {
                for (int i = 0; i < sizeA; i++) {
                    Bukkit.broadcastMessage("ELEM dbA: " + dbA.getElem(i));
                    Bukkit.broadcastMessage("ELEM dbB: " + dbB.getElem(i));
                    if (dbA.getElem(i) == dbB.getElem(i)) {
                        count = count + 1;
                    }
                }
                percentage = (count * 100) / sizeA;
            } else Bukkit.broadcastMessage("not same size " + sizeA + "..." + sizeB);
        } catch (Exception e) {
            System.out.println("Failed to compare image files ...");
        }
        return percentage;
    }

    public void newWriteImage(BufferedImage bufferedImage, int x, int y, String playerName,
                           String playerUUID, FileConfiguration config, String tableName) {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            File file=new File("plugins\\PixelFilter\\images\\tmp-" + playerName.toLowerCase() + ".png");
            FileInputStream fis = new FileInputStream(file);
            String query = "INSERT INTO `" + databaseName + "`.`" + tableName + "` (x,z,playerName,uuid,image) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, x);
            pstmt.setInt(2, y);
            pstmt.setString(3, playerName);
            pstmt.setString(4, playerUUID);
            pstmt.setBinaryStream(5, fis, (int)file.length());
            pstmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void newRetrieveImage(String tableName, String playerName) {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            File file = new File("plugins\\PixelFilter\\images\\newtmp-" + playerName.toLowerCase() + ".png");
            FileOutputStream fos = new FileOutputStream(file);
            byte b[];
            Blob blob;
            PreparedStatement ps = connection.prepareStatement("select * from " + tableName);
            ResultSet rs=ps.executeQuery();
            while(rs.next()){
                blob=rs.getBlob("image");
                b=blob.getBytes(1,(int)blob.length());
                fos.write(b);
            }
            ps.close();
            fos.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean compareImages(String tableName, BufferedImage providedImage, double maxDifference) {
        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            PreparedStatement ps = connection.prepareStatement("select * from " + tableName);
            ResultSet rs=ps.executeQuery();

            while(rs.next()){
                Random random = new Random();
                File file = new File("plugins\\PixelFilter\\images\\tmp-" + random.nextInt() + ".png");
                FileOutputStream fos = new FileOutputStream(file);
                byte b[];
                Blob blob;
                blob=rs.getBlob("image");
                b=blob.getBytes(1,(int)blob.length());
                fos.write(b);
                // compare both images, providedImage and a new bufferedimage from file
                BufferedImage iterateImage = ImageIO.read(file);
                double differencePercent = getDifferencePercent(providedImage, iterateImage);
                if(differencePercent <= maxDifference) {
                    // equal images
                    return true;
                }
            }
            // none of the images were equal
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static double getDifferencePercent(BufferedImage img1, BufferedImage img2) {
        int width = img1.getWidth();
        int height = img1.getHeight();
        int width2 = img2.getWidth();
        int height2 = img2.getHeight();
        if (width != width2 || height != height2) {
            throw new IllegalArgumentException(String.format("Images must have the same dimensions: (%d,%d) vs. (%d,%d)", width, height, width2, height2));
        }

        long diff = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                diff += pixelDiff(img1.getRGB(x, y), img2.getRGB(x, y));
            }
        }
        long maxDiff = 3L * 255 * width * height;
        return 100.0 * diff / maxDiff;
    }

    private static int pixelDiff(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xff;
        int g1 = (rgb1 >>  8) & 0xff;
        int b1 =  rgb1        & 0xff;
        int r2 = (rgb2 >> 16) & 0xff;
        int g2 = (rgb2 >>  8) & 0xff;
        int b2 =  rgb2        & 0xff;
        return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
    }

    private void removeMap(Inventory inventory, int id, Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for(int i = 0; i < inventory.getSize(); i++){
                    ItemStack itm = inventory.getItem(i);
                    if(itm != null && itm.getType().equals(Material.MAP)) {
                        if(itm.getDurability() == id) {
                            int amt = itm.getAmount() - 1;
                            itm.setAmount(amt);
                            inventory.setItem(i, amt > 0 ? itm : null);
                            inventory.addItem(new ItemStack(Material.EMPTY_MAP, 1));
                            player.updateInventory();
                            break;
                        }
                    }
                }
            }
        }.runTaskLater(plugin, 1);
    }

}