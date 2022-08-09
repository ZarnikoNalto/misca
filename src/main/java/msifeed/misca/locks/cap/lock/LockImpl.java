package msifeed.misca.locks.cap.lock;

public class LockImpl implements ILock {
    private int secret = 0;

    @Override
    public int getSecret() {
        return secret;
    }

    @Override
    public void setSecret(int value) {
        this.secret = value;
    }
}
