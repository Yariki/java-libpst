/**
 * 
 */

package example;

import com.pff.PSTActivity;
import com.pff.PSTAttachment;
import com.pff.PSTContact;
import com.pff.PSTConversationIndexData;
import com.pff.PSTException;
import com.pff.PSTFile;
import com.pff.PSTFolder;
import com.pff.PSTMessage;
import com.pff.PSTMessageStore;
import com.pff.PSTRss;
import com.pff.PSTTask;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * @author toweruser
 *
 */
public class TestGui implements ActionListener {
	private PSTFile pstFile;
	private EmailTableModel emailTableModel;
	private JTextPane emailText;
	private JPanel emailPanel;
	private JPanel attachPanel;
	private JLabel attachLabel;
	private JTextField attachText;
        
        private JPanel converasationIndexPanel;
        private JLabel conversationIndexLabel;
        private JTextField conversationIndexText;
        
        private JPanel trackingIndexPanel;
        private JLabel trackingIndexLabel;
        private JCheckBox trackingIndexText;
        
	private PSTMessage selectedMessage;
	private JFrame f;
	
	public TestGui() throws PSTException, IOException {

		// setup the basic window
        f = new JFrame("PST Browser");
		
		// attempt to open the pst file
		try {
			
			String filename = "iyariki.ya@hotmail.com.ost";
			filename = "c:\\Users\\Yariki\\AppData\\Local\\Microsoft\\Outlook\\iyariki.ya@hotmail.com.ost";
			
			pstFile = new PSTFile(filename);
			

		} catch (Exception err) {
			err.printStackTrace();
			System.exit(1);
		}
		
        
        // do the tree thing
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(pstFile.getMessageStore()); 
        try {
        	buildTree(top, pstFile.getRootFolder());
        } catch (Exception err) {
        	err.printStackTrace();
        	System.exit(1);
        }
        
        final JTree folderTree = new JTree(top){
        	public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        		DefaultMutableTreeNode nodeValue = (DefaultMutableTreeNode)value;
        		if (nodeValue.getUserObject() instanceof PSTFolder) {
        			PSTFolder folderValue = (PSTFolder)nodeValue.getUserObject();
        			
        			return folderValue.getDescriptorNodeId()+" - "+folderValue.getDisplayName()+" "+folderValue.getAssociateContentCount()+"";
        		} else if (nodeValue.getUserObject() instanceof PSTMessageStore) {
        			PSTMessageStore folderValue = (PSTMessageStore)nodeValue.getUserObject();
        			return folderValue.getDisplayName();
        		} else {
        			return value.toString();
        		}
        	}
        };
        DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(renderer.getDefaultClosedIcon());
        folderTree.setCellRenderer(renderer);
        
        // event handler for changing...
        folderTree.addTreeSelectionListener(new TreeSelectionListener() {
        	public void valueChanged(TreeSelectionEvent e) {
        		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                folderTree.getLastSelectedPathComponent();
        		if (node == null) {
        			return;
        		}
        		if (node.getUserObject() instanceof PSTFolder) {
	        		PSTFolder folderValue = (PSTFolder)node.getUserObject();
					try {
	        		selectFolder(folderValue);
					} catch(Exception err) {
						System.out.println("unable to change folder");
						err.printStackTrace();
					}
        		}
        	}
        });
        final JScrollPane treePane = new JScrollPane(folderTree);
        
        // the table
        JScrollPane emailTablePanel = null;
        try {
	        emailTableModel = new EmailTableModel(pstFile.getRootFolder(), pstFile);
	        final JTable emailTable = new JTable(emailTableModel);
	        emailTablePanel = new JScrollPane(emailTable);
        	emailTable.setFillsViewportHeight(true);
        	ListSelectionModel selectionModel = emailTable.getSelectionModel();
        	selectionModel.addListSelectionListener(new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e) {
					JTable source = emailTable;
					selectedMessage = emailTableModel.getMessageAtRow(source.getSelectedRow());
					if (selectedMessage instanceof PSTContact) {
						PSTContact contact = (PSTContact)selectedMessage;
						emailText.setText(contact.toString());
					} else if (selectedMessage instanceof PSTTask) {
						PSTTask task = (PSTTask)selectedMessage;
						emailText.setText(task.toString());
					} else if (selectedMessage instanceof PSTActivity) {
						PSTActivity journalEntry = (PSTActivity)selectedMessage;
						emailText.setText(journalEntry.toString());
					} else if (selectedMessage instanceof PSTRss) {
						PSTRss rss = (PSTRss)selectedMessage;
						emailText.setText(rss.toString());
					} else if (selectedMessage != null) {
//						System.out.println(selectedMessage.getMessageClass());
                                                String body = selectedMessage.getBody();
                                                if(body.isEmpty()){
                                                    body = selectedMessage.getBodyHTML();
                                                }
						emailText.setText(body);
                                                trackingIndexText.setSelected(selectedMessage.getConversationIndexTracking());
                                                PSTConversationIndexData indexData = selectedMessage.getConversationIndexData();
                                                UUID id = null;
                                                try {
                                                    id = indexData.getConversationUUID(selectedMessage.getConversationTopic(),selectedMessage.getConversationIndexTracking());
                                                } catch (NoSuchAlgorithmException ex) {
                                                    Logger.getLogger(TestGui.class.getName()).log(Level.SEVERE, null, ex);
                                                }
                                                if(id != null){
                                                    conversationIndexText.setText(id.toString());
                                                }else{
                                                    conversationIndexText.setText("");
                                                }
                                                
                                                
						//System.out.println(selectedMessage);
						//emailText.setText(selectedMessage.toString());
						//emailText.setText(selectedMessage.toString());
//						PSTTask task = selectedMessage.toTask();
//						emailText.setText(task.toString());
					}
					setAttachmentText();
					
//					treePane.getViewport().setViewPosition(new Point(0,0));
					emailText.setCaretPosition(0);
				}
        	});
        } catch (Exception err) {
        	err.printStackTrace();
        }
        
        
		f.setJMenuBar(createMenu());

        // the email
        emailText = new JTextPane();
        emailText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        //emailText.setFont(new Font("Arial Unicode MS", Font.PLAIN, 12));
        
        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack,BoxLayout.PAGE_AXIS));

        emailPanel = new JPanel(new BorderLayout());
        attachPanel = new JPanel(new BorderLayout());
        attachLabel = new JLabel("Attachments:");
        attachText = new JTextField("");
        attachText.setEditable(false);
        attachPanel.add(attachLabel, BorderLayout.WEST);
        attachPanel.add(attachText, BorderLayout.CENTER);
        stack.add(attachPanel);
        

        converasationIndexPanel = new JPanel(new BorderLayout());
        conversationIndexLabel = new JLabel("Conversation Index:");
        conversationIndexText = new JTextField("");
        conversationIndexText.setEditable(false);
        converasationIndexPanel.add(conversationIndexLabel,BorderLayout.WEST);
        converasationIndexPanel.add(conversationIndexText,BorderLayout.CENTER);
        stack.add(converasationIndexPanel);

        trackingIndexPanel = new JPanel(new BorderLayout());
        trackingIndexLabel = new JLabel("Index Tracking:");
        trackingIndexText = new JCheckBox();
        trackingIndexPanel.add(trackingIndexLabel,BorderLayout.WEST);
        trackingIndexPanel.add(trackingIndexText,BorderLayout.CENTER);
        stack.add(trackingIndexPanel);
        
        
        
        emailPanel.add(stack, BorderLayout.NORTH);
        emailPanel.add(emailText, BorderLayout.CENTER);

        JSplitPane emailSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, emailTablePanel, new JScrollPane(emailPanel));
        emailSplitPane.setOneTouchExpandable(true);
        emailSplitPane.setDividerLocation(0.25);
        
        
        // add a split pane, 1 for our tree, the other for our emails
        JSplitPane primaryPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePane, emailSplitPane);
        primaryPane.setOneTouchExpandable(true);
        primaryPane.setDividerLocation(0.3);
        f.add(primaryPane);
 
        // Set the default close operation for the window, 
        // or else the program won't exit when clicking close button
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
        // Set the visibility as true, thereby displaying it
        f.setVisible(true);
//        f.setSize(800, 600);
        f.setExtendedState(f.getExtendedState() | f.MAXIMIZED_BOTH);
	}
	
	private void buildTree(DefaultMutableTreeNode top, PSTFolder theFolder) {
		// this is recursive, try and keep up.
		try {
			Vector children = theFolder.getSubFolders();
			Iterator childrenIterator = children.iterator();
			while (childrenIterator.hasNext()) {
				PSTFolder folder = (PSTFolder)childrenIterator.next();

				DefaultMutableTreeNode node = new DefaultMutableTreeNode(folder);
				
				if (folder.getSubFolders().size() > 0) {
					buildTree(node, folder);
				} else {
				}
				top.add(node);
			}
		} catch (Exception err) {
			err.printStackTrace();
			System.exit(1);
		}
	}

	void setAttachmentText() {
		StringBuffer s = new StringBuffer();

		try {
			if (selectedMessage != null) {
				int numAttach = selectedMessage.getNumberOfAttachments();
				for (int x = 0; x < numAttach; x++) {
					PSTAttachment attach = selectedMessage.getAttachment(x);
					String filename = attach.getLongFilename();
					if (filename.isEmpty()) {
						filename = attach.getFilename();
					}
					if (!filename.isEmpty()) {
						if (x != 0) {
							s.append(", ");
						}
						s.append(filename);
					}
				}
			}
		} catch (Exception e) {
		}

		attachText.setText(s.toString());
	}

	void selectFolder(PSTFolder folder)
			throws IOException, PSTException
	{
		// load up the non-folder children.
		
		emailTableModel.setFolder(folder);
		
	}

	public JMenuBar createMenu() {
		JMenuBar menuBar;
		JMenu menu;

		menuBar = new JMenuBar();
		menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem("Save Attachments", KeyEvent.VK_S);
		menuItem.addActionListener(this);
		menu.add(menuItem);

		return menuBar;
	}

	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem)(e.getSource());
		if (source.getText() == "Save Attachments")
		{
			saveAttachments();
		}
	}

	private void saveAttachments() {
		if (selectedMessage != null) {
			int numAttach = selectedMessage.getNumberOfAttachments();
			if (numAttach == 0) {
				JOptionPane.showMessageDialog(f, "Email has no attachments");
				return;
			}
			try {
				for (int x = 0; x < numAttach; x++) {
					PSTAttachment attach = selectedMessage.getAttachment(x);
					InputStream attachmentStream = attach.getFileInputStream();
					String filename = attach.getLongFilename();
					if (filename.isEmpty()) {
						filename = attach.getFilename();
					}
					JFileChooser chooser = new JFileChooser();
					chooser.setSelectedFile(new File(filename));
					int r = chooser.showSaveDialog(f);
					if (r == JFileChooser.APPROVE_OPTION) {
						FileOutputStream out = new FileOutputStream(chooser.getSelectedFile());
						// 8176 is the block size used internally and should give the best performance
						int bufferSize = 8176;
						byte[] buffer = new byte[bufferSize];
						int count;
						do {
							count = attachmentStream.read(buffer);
							out.write(buffer, 0, count);
						} while (count == bufferSize);
						out.close();
					}
					attachmentStream.close();
				}
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(f, "Failed writing to file");
			} catch (PSTException pste) {
				JOptionPane.showMessageDialog(f, "Error in PST file");
			}
		}
	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws PSTException 
	 */
	public static void main(String[] args) throws PSTException, IOException {
		new TestGui();
	}

}

class EmailTableModel extends AbstractTableModel {
	
	PSTFolder theFolder = null;
	PSTFile theFile = null;
	
	HashMap cache = new HashMap();
	
	public EmailTableModel(PSTFolder theFolder, PSTFile theFile) {
		super();
		
		this.theFolder = theFolder;
		this.theFile = theFile;
	}
	
	String[] columnNames = {
    		"Descriptor ID",	
    		"Subject",
    		"From",
    		"To",
    		"Date",
    		"Has Attachments"
	};
	String[][] rowData = {{"","","","",""}};
	int rowCount = 0;
	public String getColumnName(int col) {
        return columnNames[col].toString();
    }
    public int getColumnCount() { return columnNames.length; }
    
    public int getRowCount() { 
    	try {
			//System.out.println("Email count: "+theFolder.getEmailCount());
    		return theFolder.getContentCount();
    	} catch (Exception err) {
    		err.printStackTrace();
    		System.exit(0);
    	}
    	return 0;
    }
    
    public PSTMessage getMessageAtRow(int row) {
    	PSTMessage next = null;
		try {
	    	if (cache.containsKey(row)) {
				next = (PSTMessage)cache.get(row);
			} else {
	    		theFolder.moveChildCursorTo(row);
				next = (PSTMessage)theFolder.getNextChild();
	    		cache.put(row, next);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return next;
    }
    
    
    public Object getValueAt(int row, int col) {
    	// get the child at...
    	try {
			PSTMessage next = getMessageAtRow(row);

			if (next == null) {
				return null;
			}
    		
			switch (col) {
				case 0:
					return next.getDescriptorNode().descriptorIdentifier+"";
				case 1:
					return next.getSubject();
				case 2:
					return next.getSentRepresentingName() + " <"+ next.getSentRepresentingEmailAddress() +">";
				case 3:
					return next.getReceivedByName() + " <"+next.getReceivedByAddress()+">" + 
						next.getDisplayTo();
				case 4:
					return next.getClientSubmitTime();
//					return next.isFlagged();
//					return next.isDraft();
//					PSTTask task = next.toTask();
//					return task.toString();
				case 5:
					return (next.hasAttachments() ? "Yes" : "No");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
    	
        return "";
    }
    public boolean isCellEditable(int row, int col) { return false; }
    
    public void setFolder(PSTFolder theFolder)
			throws PSTException, IOException
	{
    	theFolder.moveChildCursorTo(0);
    	this.theFolder = theFolder;
    	cache = new HashMap();
    	this.fireTableDataChanged();
    }

}
