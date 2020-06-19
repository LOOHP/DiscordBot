package com.loohp.discordbot;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class StickerManager {
	
	private static File DataFolder = new File("stickers");
	private static File TempFolder = new File("stickers_temp");
	private static int BUFFER_SIZE = 4096;
	
	public static File getSticker(String id) {
		if (id.equalsIgnoreCase("readme")) {
			return null;
		}
		for (File file : DataFolder.listFiles()) {
			if (file.getName().substring(0, file.getName().lastIndexOf(".")).equalsIgnoreCase(id)) {
				return file;
			}
		}
		generate();
		for (File file : DataFolder.listFiles()) {
			if (file.getName().substring(0, file.getName().lastIndexOf(".")).equalsIgnoreCase(id)) {
				return file;
			}
		}
		return null;
	}
	
	public static String getStickerList() {
		generate();
		StringBuilder sb = new StringBuilder();
		for (File file : DataFolder.listFiles()) {
			if (!file.getName().substring(file.getName().lastIndexOf(".") + 1).equalsIgnoreCase("md")) {
				sb.append(file.getName().substring(0, file.getName().lastIndexOf(".")).toLowerCase() + "\n");
			}
		}
		return sb.toString();
	}
	
	public static void generate() {
		try {
			removeFolder(TempFolder);
		
			//https://github.com/LOOHP/StickerBase/archive/master.zip
			TempFolder.mkdirs();
			
			File zip = downloadFile(new File(TempFolder, "stickers.zip"), new URL("https://github.com/LOOHP/StickerBase/archive/master.zip"));
			if (zip == null) {
				try {
					removeFolder(TempFolder);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;
			}
			
			removeFolder(DataFolder);
			extract(new ZipInputStream(new FileInputStream(zip)), new File("").getAbsoluteFile());
			new File("StickerBase-master").renameTo(new File("stickers"));
							
			removeFolder(TempFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void removeFolder(File folder) {
		if (folder.exists()) {
			File[] files = folder.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					removeFolder(file);
				} else {
					file.delete();
				}
			}
			folder.delete();
		}
	}
	
	public static File downloadFile(File output, URL download) {
	    try {
	        ReadableByteChannel rbc = Channels.newChannel(download.openStream());

	        FileOutputStream fos = new FileOutputStream(output);

	        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

	        fos.close();

	        return output;
	    }
	    catch (FileNotFoundException e) {
	        e.printStackTrace();
	    }
	    catch (MalformedURLException e) {
	        e.printStackTrace();
	    }
	    catch (IOException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public static void extract(ZipInputStream zip, File target) throws IOException {
        try {
            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                File file = new File(target, entry.getName());

                if (!file.toPath().normalize().startsWith(target.toPath())) {
                    throw new IOException("Bad zip entry");
                }

                if (entry.isDirectory()) {
                    file.mkdirs();
                    continue;
                }

                byte[] buffer = new byte[BUFFER_SIZE];
                file.getParentFile().mkdirs();
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                int count;

                while ((count = zip.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }

                out.close();
            }
        } finally {
            zip.close();
        }
    }

}
