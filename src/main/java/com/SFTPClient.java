package com;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class SFTPClient {

    private String host;
    private int port;
    private String username;
    private String password;
    private Session session;
    private Channel channel;
    private ChannelSftp sftpChannel;

    public SFTPClient(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void connect() {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no"); 
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;
            System.out.println("Connected successfully to SFTP server.");
        } catch (Exception e) {
            System.out.println("An error occurred while attempting to connect to the SFTP server: " + e.getMessage());    
        }
    }

    public void disconnect() {
        if (sftpChannel != null) {
            sftpChannel.exit();
        }
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
        System.out.println("Disconnected from SFTP server.");
    }

    public void uploadFile(String localFilePath, String remoteFilePath) {
    File localFile = new File(localFilePath);
    if (!localFile.exists()) {
        System.out.println("Local file does not exist: " + localFilePath);
        return;
    }

    try {
        sftpChannel.put(localFilePath, remoteFilePath);
        System.out.println("Upload successful.");
    } catch (SftpException e) {
        if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
            System.out.println("Remote directory does not exist: " + remoteFilePath);
        } else {
            System.out.println("Upload failed: " + e.getMessage());
        }
    } catch (Exception e) {
        System.out.println("An error occurred while attempting to upload the file: " + e.getMessage());
    }
}

public void downloadFile(String remoteFilePath, String localFilePath) {
    // Before attempting the download, ensure the local directory exists
    File localFile = new File(localFilePath);
    File parentDir = localFile.getParentFile();
    if (!parentDir.exists()) {
        if (!parentDir.mkdirs()) {
            System.out.println("Failed to create local directory: " + parentDir.getPath());
            return;
        }
    }

    try {
        sftpChannel.get(remoteFilePath, localFilePath);
        System.out.println("Download successdownlful.");
    } catch (SftpException e) {
        if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
            System.out.println("Remote file does not exist: " + remoteFilePath);
        } else {
            System.out.println("Download failed: " + e.getMessage());
        }
    } catch (Exception e) {
        System.out.println("An error occurred while attempting to download the file: " + e.getMessage());
    }
}

    public static void main(String[] args) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Enter SFTP details:");
            System.out.print("Host: ");
            String host = br.readLine();
            System.out.print("Port: ");
            int port = Integer.parseInt(br.readLine());
            System.out.print("Username: ");
            String username = br.readLine();
            System.out.print("Password: ");
            String password = br.readLine();

            SFTPClient client = new SFTPClient(host, port, username, password);
            client.connect();

            while (true) {
                System.out.println("Enter command (upload, download, exit):");
                String command = br.readLine();
                if ("exit".equalsIgnoreCase(command)) {
                    break;
                } else if ("upload".equalsIgnoreCase(command)) {
                    System.out.print("Enter local file path: ");
                    String localPath = br.readLine();
                    System.out.print("Enter remote file path: ");
                    String remotePath = br.readLine();
                    client.uploadFile(localPath, remotePath);
                } else if ("download".equalsIgnoreCase(command)) {
                    System.out.print("Enter remote file path: ");
                    String remotePath = br.readLine();
                    System.out.print("Enter local file path: ");
                    String localPath = br.readLine();
                    client.downloadFile(remotePath, localPath);
                } else {
                    System.out.println("Unknown command.");
                }
            }

            client.disconnect();
        } catch (Exception e) {
    
        }
    }
}
