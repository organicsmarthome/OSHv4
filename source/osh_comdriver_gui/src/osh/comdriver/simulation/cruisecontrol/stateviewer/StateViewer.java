package osh.comdriver.simulation.cruisecontrol.stateviewer;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import osh.datatypes.registry.StateExchange;


/**
 * 
 * @author Till Schuberth
 *
 */
public class StateViewer extends JPanel implements ItemListener {
	private static final long serialVersionUID = 2299684852859082725L;
	
	private JComboBox<String> registrycombo, typecombo;
	private DefaultComboBoxModel<String> typesmodel;
	private TreeSet<Class<? extends StateExchange>> oldTypesList = new TreeSet<Class<? extends StateExchange>>(new ClassNameComparator());
	private JTable table;
	private StatesTableModel model;
	private Set<StateViewerListener> listeners = new HashSet<>();


	/**
	 * CONSTRUCTOR
	 */
	public StateViewer() {
		super();
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		registrycombo = new JComboBox<String>(toStringArray(StateViewerRegistryEnum.values()));
		registrycombo.setMaximumSize(new Dimension(Short.MAX_VALUE, registrycombo.getPreferredSize().height));
		registrycombo.setSelectedIndex(0);
		registrycombo.addItemListener(this);
		add(registrycombo);
		typesmodel = new DefaultComboBoxModel<>();
		typecombo = new JComboBox<>(typesmodel);
		typecombo.setMaximumSize(new Dimension(Short.MAX_VALUE, typecombo.getPreferredSize().height));
		typecombo.addItemListener(this);
		add(typecombo);
		model = new StatesTableModel();
		table = new JTable(model);
		table.getColumnModel().getColumn(0).setPreferredWidth(150);
		table.getColumnModel().getColumn(1).setPreferredWidth(50);
		table.getColumnModel().getColumn(2).setPreferredWidth(500);
		JScrollPane sp = new JScrollPane(table);
		sp.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		sp.setAlignmentX(CENTER_ALIGNMENT);
		add(sp);
		
		this.setPreferredSize(new Dimension(200, 200));
	}

	private static String[] toStringArray(Object[] arr) {
		if (arr == null) throw new NullPointerException("argument is null");
		
		String[] ret = new String[arr.length];
		for (int i = 0; i < arr.length; i++) {
			ret[i] = arr[i].toString();
		}
		
		return ret;
	}

	
	public void showTypes(Set<Class<? extends StateExchange> > types) {
		synchronized (oldTypesList) {
			
			for (Class<? extends StateExchange> c : types) {
				if (!oldTypesList.contains(c)) {
					oldTypesList.add(c);
					typesmodel.insertElementAt(c.getName(), getIndexOfInOldList(c));
				}
			}

			Set<Class<? extends StateExchange> > toremove = new HashSet<>();
			for (Class<? extends StateExchange> c : oldTypesList) {
				if (!types.contains(c)) {
					typesmodel.removeElement(c.getName());
					toremove.add(c);
				}
			}
			oldTypesList.removeAll(toremove);
		}
	}

	private int getIndexOfInOldList(Class<? extends StateExchange> type) {
		int index = 0;
		synchronized (oldTypesList) {
			for (Class<? extends StateExchange> c : oldTypesList) {
				if (c.equals(type)) return index;
				index++;
			}
		}
		
		return -1;
	}

	public void showStates(Map<UUID, ? extends StateExchange> entries) {
		model.setData(entries);
	}
	
	public void registerListener(StateViewerListener l) {
		synchronized (listeners) {
			if (l != null) listeners.add(l);
		}
	}
	
	public void unregisterListener(StateViewerListener l) {
		synchronized (listeners) {
			if (l != null) listeners.remove(l);
		}
	}
	
	private void notifyListenersState(Class<? extends StateExchange> cls) {
		synchronized (listeners) {
			for (StateViewerListener l : listeners) {
				l.stateViewerClassChanged(cls);
			}
		}
	}

	private void notifyListenersRegistry(StateViewerRegistryEnum registry) {
		synchronized (listeners) {
			for (StateViewerListener l : listeners) {
				l.stateViewerRegistryChanged(registry);
			}
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == registrycombo) {
			notifyListenersState(null);
			showTypes(new HashSet<Class<? extends StateExchange> >());
			notifyListenersRegistry(StateViewerRegistryEnum.findByString(e.getItem().toString()));
		} else if (e.getSource() == typecombo) {
			Class<? extends StateExchange> cls = null;

			synchronized (oldTypesList) {
				for (Class<? extends StateExchange> c : oldTypesList) {
					if (c.getName().equals(e.getItem())) {
						cls = c;
						break;
					}
				}
			}

			synchronized (this) {
				notifyListenersState(cls);
			}
		}
	}
	
}
