package pl.maxmati.tobiasz.mmos.bread.api.charge;

import java.util.Date;

import pl.maxmati.tobiasz.mmos.bread.api.user.User;

/**
 * Created by mmos on 21.02.16.
 *
 * @author mmos
 */
public class Charge {
    private final String name;
    private final Date date;
    private final Integer[] to;
    private final String rawAmount;

    public String getName() {
        return name;
    }

    public Date getDate() {
        return date;
    }

    public Integer[] getTo() {
        return to;
    }

    public double getRawAmount() {
        return Double.parseDouble(rawAmount);
    }

    public Charge(String name, Date date, User[] to, double rawAmount) {
        this.name = name;
        this.date = date;
        this.rawAmount = Double.toString(rawAmount);
        this.to = new Integer[to.length];
        for(int i = 0; i < to.length; ++i)
            this.to[i] = to[i].getId();
    }
}
