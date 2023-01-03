public class book {
	public enum State {
		ONSHELF, INITIAL, ONLOAN, TERMINAL
	}

	private State state;
	private int fine;

	public book() {
		state = INITIAL;
	}

	public void bought(){
		if(initial==true) {
			 state = ONSHELF
		}
	}

	public void borrowed(){
		if(onShelf==true) {
			 state = ONLOAN
		}
	}

	public void returned(){
		if(onLoan==true) {
			 state = ONSHELF
		}
	}

	public void lost(){
		if(onLoan==true) {
			 state = TERMINAL
		}
	}

	public void disposed(){
		if(onShelf==true) {
			 state = TERMINAL
		}
	}

	public void renew(){
		if(onLoan==true && fine>5) {
			 state = ONLOAN
		}
	}
}