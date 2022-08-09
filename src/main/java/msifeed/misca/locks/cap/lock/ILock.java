package msifeed.misca.locks.cap.lock;

public interface ILock {
    int getSecret();

    void setSecret(int value);
}
