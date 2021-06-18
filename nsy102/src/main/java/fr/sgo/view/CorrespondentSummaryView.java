package fr.sgo.view;

import java.util.Observable;

import fr.sgo.controller.ActionHandler;
import fr.sgo.entity.Correspondent;

/**
 * Class CorrespondentSummaryView
 * 
 * A panel representing a correspondent with action button
 *
 * @author Stéfan Georgesco
 * @version 1.0
 */
@SuppressWarnings("deprecation")
public class CorrespondentSummaryView extends SummaryView {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2046950170711645517L;
	private Correspondent correspondent;

	public CorrespondentSummaryView(ActionHandler actionHandler, Correspondent correspondent) {
		super(actionHandler);
		this.correspondent = correspondent;
		this.correspondent.addObserver(this);
		refresh();
	}

	public Correspondent getCorrespondent() {
		return correspondent;
	}

	@Override
	public void refresh() {
		refresh(correspondent.getUserName(), correspondent.isOnline());
	}

	@Override
	public void update(Observable observable, Object arg) {
		if (observable instanceof Correspondent) {
			Correspondent c = (Correspondent) observable;
			if (c.equals(correspondent))
				refresh();
		}
	}
}
