import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import java.awt.event.*;
import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.Random;

class Controller extends mainForm{
    private JFrame frame;
    private String path1;
    private String path2;

    void start(String frameName){
        initFrame(frameName);
        bindEvents();
    }

    private void initFrame(String frameName){
        frame = new JFrame(frameName);

        frame.getContentPane().add(framePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1280,720);
        frame.setVisible(true);
    }

    private  void bindEvents(){
        addBtn1.addActionListener(new AddFile(1));
        addBtn2.addActionListener(new AddFile(2));
        codeC1.addActionListener(new SetEncoding(1));
        codeC2.addActionListener(new SetEncoding(2));
        blendBtn.addActionListener(new Blend());
        exportBtn.addActionListener(new ExportFile());

        link.addMouseListener(new OpenLink());

        text1.getDocument().addDocumentListener(new UpdateBlockNum(1));
        text2.getDocument().addDocumentListener(new UpdateBlockNum(2));
    }
    private String[] getBlocks(int type){
        JTextArea textArea;
        String splitter;
        if( type == 1 ){
            textArea = text1;
            splitter = splitter1.getText()==null ? "\n" : splitter1.getText();
        }else{
            textArea = text2;
            splitter = splitter2.getText()==null ? "\n" : splitter2.getText();
        }

        String[] temp;
        int lineNum = countBlocks(type);
        if( lineNum == 0 ){
            temp = null;
        }else if( lineNum == 1 ){
            temp = new String[1];
            temp[0] = textArea.getText();
        }else{
            temp = textArea.getText().split(splitter);
        }

        return temp;
    }

    private int countBlocks(int type){
        JTextArea textArea;
        String splitter;
        if( type == 1 ){
            textArea = text1;
            splitter = splitter1.getText().equals("") ? "\n" : splitter1.getText();
        }else{
            textArea = text2;
            splitter = splitter2.getText().equals("") ? "\n" : splitter2.getText();
        }

        String[] stringArray = textArea.getText().split(splitter);

        return stringArray.length;
    }

    private void setBlockNum(int type){
        JLabel label;
        if( type == 1 ){
            label = blockNum1;
        }else{
            label = blockNum2;
        }

        label.setText(Integer.toString(countBlocks(type))+"块");
    }

    private void alert(String info){
        JOptionPane.showOptionDialog(null,
                info,
                "警告",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,null,null);
    }

    private void fillText(JTextArea textArea,String path,String encoding){
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
            FileSystemView fsv = FileSystemView.getFileSystemView();

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
            String[] fileBlocks1 = getBlocks(1);
            String[] fileBlocks2 = getBlocks(2);


            if( fileBlocks1==null || fileBlocks2==null ){
                alert("文件内容不能为空！");
            }else{
                if( fileBlocks1.length!=fileBlocks2.length ){
                    alert("两个文件的块数不同，合成的结果可能不符合预期！");
                }

                int length = fileBlocks1.length<fileBlocks2.length ? fileBlocks1.length : fileBlocks2.length;
                Random random = new Random();

                text3.setText("");
                for( int i = 0 ; i < length ; i++ ){
                    if( random.nextBoolean() ){
                        text3.setText(text3.getText()+fileBlocks1[i]);
                        if( fileBlocks1[i].indexOf('\n') < 0 ) {
                            text3.setText(text3.getText() + '\n');
                        }
                    }else{
                        text3.setText(text3.getText()+fileBlocks2[i]);
                        if( fileBlocks2[i].indexOf('\n') < 0 ) {
                            text3.setText(text3.getText() + '\n');
                        }
                    }
                }
            }

        }
    }

    class UpdateBlockNum implements DocumentListener {
        int type;

        UpdateBlockNum(int type){
            this.type = type;
        }
        public void changedUpdate(DocumentEvent e) {
            setBlockNum(type);
        }
        public void insertUpdate(DocumentEvent e) {
            setBlockNum(type);
        }
        public void removeUpdate(DocumentEvent e) {
            setBlockNum(type);
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
