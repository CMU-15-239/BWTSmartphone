#Braile Writing Tutor for Smartphone
####A project for 15-239
-----------------------------------

Using the BWT in your own code projects is super easy!  
Just import BWT and add the following to your activity:

    public class MainActivity extends Activity{
        // Create a BWT instance
        private BWT bwt = new BWT(this, MainActivity.this);
    
        // Initialize the BWT.
        protected void onCreate(Bundle b){
            super.onCreate(b);
            bwt.init();
        }
    
        // Kill the USB connection when not in use.
        protected void onPause(){
            super onPause();
            bwt.stop();
        }
    
        // Start the USB connection whe needed.
        protected void onResume(){
            super.onResume();
            bwt.start();
        }
    }
