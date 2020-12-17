package webserver.webserver;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WebGUI {
    private JButton startServerButton;
    private JPanel MainPanel;
    private JButton serverMaintenanceButton;
    private JButton stopServerButton;
    public static String status = "Stopped";

    public WebGUI() {
        startServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,"Starting Webserver");
            }
        });
        stopServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,"Stopping Webserver");
            }
        });
        serverMaintenanceButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null,"Starting Maintenance");
            }
        });
        startServerButton = new JButton("Start server");
        startServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if(startServerButton.getText().equals("Start server"))
                {
                    startServerButton.setText("Server stopped");
                    status="Stopped";
                }
                else
                {
                    startServerButton.setText("Start server");
                    status="Running";
                }

            }
        });
        startServerButton.setEnabled(true);

        stopServerButton = new JButton("Stop server");
        stopServerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopServerButton.setText("Server stopped");
                status="Stopped";
            }
        });
        stopServerButton.setEnabled(true);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("WebGUI");
        frame.setContentPane(new WebGUI().MainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}