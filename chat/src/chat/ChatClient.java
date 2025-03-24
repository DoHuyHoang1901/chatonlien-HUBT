import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClient {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12345;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private String username;

    public ChatClient() {
        setupUsername(); // Lấy username trước
        setupUI();
        connectToServer();
    }

    // Phương thức để lấy username trước khi khởi tạo giao diện
    private void setupUsername() {
        JTextField userField = new JTextField(15);
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(new JLabel("👤 Nhập tên của bạn:"), BorderLayout.NORTH);
        panel.add(userField, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
            null, 
            panel, 
            "Đăng nhập", 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION && !userField.getText().trim().isEmpty()) {
            username = userField.getText().trim();
        } else {
            username = "User" + (int) (Math.random() * 1000);
            JOptionPane.showMessageDialog(null, "Bạn đã được gán tên mặc định: " + username, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void setupUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Không thể thiết lập giao diện hệ thống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }

        JFrame frame = new JFrame("💬 Chat Online");
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(5, 5));

        // Panel chính với nền đơn giản
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 240));

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        headerPanel.setBackground(new Color(230, 230, 230));
        JLabel titleLabel = new JLabel(" Chat Room");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerPanel.add(titleLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Khu vực chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Panel input với emoji
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBackground(new Color(240, 240, 240));

        // Thanh emoji
        JPanel emojiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        emojiPanel.setBackground(new Color(230, 230, 230));
        addEmojiButtons(emojiPanel);
        inputPanel.add(emojiPanel, BorderLayout.NORTH);

        // Input field
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1, true));
        inputPanel.add(inputField, BorderLayout.CENTER);

        // Nút gửi
        sendButton = new JButton("Gửi 📤");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        inputPanel.add(sendButton, BorderLayout.EAST);

        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        frame.add(mainPanel);

        // Xử lý sự kiện
        inputField.addActionListener(e -> sendMessage());
        sendButton.addActionListener(e -> sendMessage());

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Thêm các nút emoji
    private void addEmojiButtons(JPanel panel) {
        String[] emojis = {"😊", "😂", "😍", "😢", "👍", "👎"};
        for (String emoji : emojis) {
            JButton emojiButton = new JButton(emoji);
            emojiButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            emojiButton.setMargin(new Insets(2, 2, 2, 2));
            emojiButton.setContentAreaFilled(false);
            emojiButton.addActionListener(e -> inputField.setText(inputField.getText() + emoji));
            panel.add(emojiButton);
        }
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Gửi username ngay khi kết nối
            if (username != null) {
                out.println("/username " + username);
            }

            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        chatArea.append(message + "\n");
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null, "Mất kết nối với server.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }).start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Không thể kết nối đến Server tại " + SERVER_IP + ":" + SERVER_PORT + 
                ". Vui lòng kiểm tra server.", "Lỗi kết nối", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty() && username != null) {
            try {
                if (out != null) {
                    out.println(username + ": " + message);
                    inputField.setText("");
                } else {
                    JOptionPane.showMessageDialog(null, "Chưa kết nối đến server.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Lỗi khi gửi tin nhắn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } else if (username == null) {
            JOptionPane.showMessageDialog(null, "Vui lòng nhập tên trước khi gửi tin nhắn.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClient::new);
    }
}