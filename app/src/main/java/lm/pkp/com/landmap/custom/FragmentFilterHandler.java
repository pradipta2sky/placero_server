package lm.pkp.com.landmap.custom;

import java.util.List;

/**
 * Created by USER on 11/24/2017.
 */
public interface FragmentFilterHandler {

    public void doFilter(List<String> filterables, List<String> executables);

    public void resetFilter();
}
