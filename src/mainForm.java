import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.*;
import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.Random;


public class mainForm {
    private JPanel framePanel;
    private JButton addBtn1;
    private JButton addBtn2;
    private JButton blendBtn;
    private JPanel panel1;
    private JPanel panel2;
    private JPanel panel3;
    private JTextArea text1;
    private JTextArea text2;
    private JTextArea text3;
    private JLabel link;
    private JButton exportBtn;
    private JLabel line1;
    private JLabel line2;
    private JButton codeC1;
    private JButton codeC2;
    private JFrame frame;
    private String[] fileLines1;
    private String[] fileLines2;
    private String path1;
    private String path2;

    public static void main(String[] args){
        new mainForm().start();
    }

    void start(){
        initFrame("文件混合器");
        bindEvents();
    }

    void initFrame(String frameName){
        frame = new JFrame(frameName);

        frame.getContentPane().add(framePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,600);
        frame.setVisible(true);
    }

    void bindEvents(){
        addBtn1.addActionListener(new AddFile(1));
        addBtn2.addActionListener(new AddFile(2));
        codeC1.addActionListener(new SetEncoding(1));
        codeC2.addActionListener(new SetEncoding(2));
        blendBtn.addActionListener(new Blend());
        exportBtn.addActionListener(new ExportFile());

        link.addMouseListener(new OpenLink());

        text1.getDocument().addDocumentListener(new UpdateLines(text1,line1));
        text2.getDocument().addDocumentListener(new UpdateLines(text2,line2));
    }

    String[] getLines(JTextArea textArea){
        String[] temp;
        int lineNum = countLines(textArea);
        if( lineNum == 0 ){
            temp = null;
        }else if( lineNum == 1 ){
            temp = new String[1];
            temp[0] = textArea.getText();
        }else{
            temp = textArea.getText().split("\n");
        }

        return temp;
    }

    int countLines(JTextArea textArea){
        int num = 0;
        char[] charArray = textArea.getText().toCharArray();

        for( char item : charArray ){
            if( item == '\n' ){
                num++;
            }
        }

        if( charArray.length>0 ){
            num++;
        }

        return num;
    }

    void setLineNum(JTextArea textArea,JLabel label){
        label.setText(Integer.toString(countLines(textArea))+"行");
    }

    void alert(String info){
        JOptionPane.showOptionDialog(null,
                info,
                "警告",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,null,null);
    }

    void fillText(JTextArea textArea,String path,String encoding){
        File file = new File(path);
        textArea.setText("");

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file),encoding));

            String line;
            while( (line=bufferedReader.readLine()) != null){
                textArea.setText(textArea.getText()+line+'\n');
            }

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class OpenLink implements MouseListener{
        public void mouseClicked(MouseEvent e) {
            try {
                Desktop.getDesktop().browse(new URI("https://github.com/joenahm"));
            } catch (Exception e1) { e.paramString();}
        }
        public void mouseEntered(MouseEvent e) {
            link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            link.setToolTipText("点击以访问作者GitHub主页");
        }
        public void mousePressed(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
    }

    class AddFile implements ActionListener {
        private int type;

        AddFile(int type){
            this.type = type;
        }

        public void actionPerformed(ActionEvent event) {
            JFileChooser fileChooser = new JFileChooser();
            FileSystemView fsv = FileSystemView.getFileSystemView();  //注意了，这里重要的一句

            fileChooser.setCurrentDirectory(fsv.getHomeDirectory());
            fileChooser.setDialogTitle("请选择要添加的文件");
            fileChooser.setApproveButtonText("确定");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            int result = fileChooser.showOpenDialog(frame);
            if (JFileChooser.APPROVE_OPTION == result) {
                String path=fileChooser.getSelectedFile().getPath();

                JTextArea textArea;
                if( type == 1 ){
                    textArea = text1;
                    path1 = path;
                }else{
                    textArea = text2;
                    path2 = path;
                }

                fillText(textArea,path,"UTF-8");
            }
        }
    }

    class Blend implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            fileLines1 = getLines(text1);
            fileLines2 = getLines(text2);


            if( fileLines1==null || fileLines2==null ){
                alert("文件内容不能为空！");
            }else{
                if( fileLines1.length!=fileLines2.length ){
                    alert("两个文件的行数不同，合成的结果可能不符合预期！");
                }

                int length = fileLines1.length<fileLines2.length ? fileLines1.length : fileLines2.length;
                Random random = new Random();

                text3.setText("");
                for( int i = 0 ; i < length ; i++ ){
                    if( random.nextBoolean() ){
                        text3.setText(text3.getText()+fileLines1[i]);
                        text3.setText(text3.getText()+'\n');
                    }else{
                        text3.setText(text3.getText()+fileLines2[i]);
                        text3.setText(text3.getText()+'\n');
                    }
                }
            }

        }
    }

    class UpdateLines implements DocumentListener {
        private JTextArea textArea;
        private JLabel label;

        UpdateLines(JTextArea textArea,JLabel label){
            this.textArea = textArea;
            this.label = label;
        }
        public void changedUpdate(DocumentEvent e) {
            setLineNum(textArea,label);
        }
        public void insertUpdate(DocumentEvent e) {
            setLineNum(textArea,label);
        }
        public void removeUpdate(DocumentEvent e) {
            setLineNum(textArea,label);
        }
    }

    class SetEncoding implements ActionListener{
        private int type;
        private int status = 0;

        SetEncoding(int type){
            this.type = type;
        }

        public void actionPerformed(ActionEvent e) {
            String encoding;
            if( status%2 == 0 ){
                encoding = "GBK";
            }else{
                encoding = "UTF-8";
            }
            status++;

            if( type == 1 ){
                fillText(text1,path1,encoding);
            }else{
                fillText(text2,path2,encoding);
            }
        }
    }

    class ExportFile implements ActionListener{
        public void actionPerformed(ActionEvent event) {
            FileNameExtensionFilter filter=new FileNameExtensionFilter("*.txt","txt");
            JFileChooser fc=new JFileChooser();
            fc.setFileFilter(filter);
            fc.setMultiSelectionEnabled(false);
            int result=fc.showSaveDialog(frame);
            if( result == JFileChooser.APPROVE_OPTION){
                File file=fc.getSelectedFile();
                if (!file.getPath().endsWith(".txt")) {
                    file=new File(file.getPath()+".txt");
                }

                try {
                    FileWriter fileWriter = new FileWriter(file);
                    if (!file.exists()) {
                        file.createNewFile();
                    }

                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write(text3.getText());

                    bufferedWriter.close();
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
