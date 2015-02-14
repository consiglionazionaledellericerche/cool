package it.cnr.cool.repository;

/**
 * Created by francesco on 2/14/15.
 */
public interface PermissionRepository {

    String getRbac();

    boolean update(String s);
}
