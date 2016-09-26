package osh.comdriver.simulation.cruisecontrol;

import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 * 
 * @author Till Schuberth, Ingo Mauser
 *
 */
class CruiseControl extends JPanel {

	private static final long serialVersionUID = 1L;
	private final DateFormat timeformatter;
	
	private boolean wait = true;
	private boolean update = true;
	private long go = 0;
	private volatile long currentTime = 0;
	private final boolean waitallowed;
	
	private JLabel l_time = new JLabel();

	
	/**
	 * CONSTRUCTOR
	 */
	public CruiseControl(boolean waitallowed) {
		super();
		
		this.waitallowed = waitallowed;
		
		timeformatter = DateFormat.getDateTimeInstance();
		timeformatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		FlowLayout layout = new FlowLayout();
		this.setLayout(layout);
		
		l_time.setText("-- time --");
		add(l_time);
		
		final Checkbox c_wait = new Checkbox("wait (global scheduler)");
		c_wait.setState(true);
		c_wait.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				wait = c_wait.getState();
			}
		});
		if (!waitallowed) {
			c_wait.setEnabled(false);
			c_wait.setState(false);
			wait = false;
		}
		this.add(c_wait);
		

		final Checkbox c_update = new Checkbox("update");
		c_update.setState(true);
		c_update.addItemListener(new ItemListener() { 

			@Override
			public void itemStateChanged(ItemEvent e) {
				update = c_update.getState();
				if (update) {
					c_wait.setState(true);
					wait = true;
				}
			}
			
		});
		this.add(c_update);
		
		JButton b = new JButton("go");
		b.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (CruiseControl.this) {
					go = -1;
				}
			}
			
		});
		this.add(b);
		
		final JTextField tf = new JTextField(10);
		final Color normalColor = tf.getForeground();
		tf.getDocument().addDocumentListener(new DocumentListener() {
			
			private void checkInput() {
				try {
					Long.parseLong(tf.getText());
					//parsed correctly
					tf.setForeground(normalColor);
				} 
				catch (NumberFormatException e) {
					tf.setForeground(Color.RED);
				}
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				checkInput();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				checkInput();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				checkInput();
			}
		});
		this.add(tf);
		
		b = new JButton("go to time");
		b.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				synchronized (CruiseControl.this) {
					try {
						go = Long.parseLong(tf.getText());
					} catch (NumberFormatException e1) {}
					c_wait.setEnabled(true);
				}
			}
		});
		this.add(b);

		this.setPreferredSize(new Dimension(300, 200));
	}

	public boolean isWait() {
		if (!waitallowed) return false;
		return wait;
	}

	public boolean isUpdate() {
		return update;
	}
	
	public void waitForGo() {
		while (true) {
			synchronized (this) {
				if (go < 0 || go > currentTime) {
					if (go < 0) go = 0; // wait next time again
					return;
				}
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
	}

	public void updateTime(long timestamp) {
		l_time.setText(new Long(timestamp).toString() + " (" + timeformatter.format(new Date(timestamp * 1000)) + " UTC)");
		currentTime = timestamp;
	}

}
