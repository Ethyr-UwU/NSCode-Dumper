package pl.korneldrabent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public final class NSCodeLoaderDuper {
  private static final Gson GSON = new GsonBuilder()
      .disableHtmlEscaping()
      .create();
  private static final String BASE_URL = "http://api.code.nssv.pl:1337";
  private static final String DOWNLOAD_INFO_URL = BASE_URL + "/plugin/%s/%s/true";
  private static final String DOWNLOAD_URL = BASE_URL + "/download/%s";
  private static final String DOWNLOAD_USER_AGENT = "xNSCodeLoader";
  private static final String ENCODED_INFO_FORMAT = "%s %s %s %s";

  public static void main(String[] args) throws IOException {
    if (args.length != 4) {
      System.err.println("java -jar xNSCodeLoader-duper.jar (ip serwera) (port serwera) (licencja) (suma kontrolna pliku server.properties (MD5))");
      return;
    }

    String encodedServerInfo = Base64.getEncoder().encodeToString((String.format(ENCODED_INFO_FORMAT, args[0], args[1], args[3], args[1])).getBytes());
    JsonObject jsonObject = GSON.fromJson(IOUtils.toString(URI.create(String.format(DOWNLOAD_INFO_URL, args[2], encodedServerInfo)), StandardCharsets.UTF_8), JsonObject.class);
    if (jsonObject.has("error")) {
      System.err.println(jsonObject.get("error").getAsString());
      return;
    }

    if (!jsonObject.get("status").getAsBoolean()) {
      System.err.println("nie mozna w tej chwili pobrac tego produktu :(");
      return;
    }

    URLConnection connection = new URL(String.format(DOWNLOAD_URL, jsonObject.get("download").getAsString())).openConnection();
    connection.setRequestProperty("User-Agent", DOWNLOAD_USER_AGENT);
    connection.connect();
    FileUtils.copyInputStreamToFile(connection.getInputStream(), new File(args[2] + ".jar"));
  }

}
