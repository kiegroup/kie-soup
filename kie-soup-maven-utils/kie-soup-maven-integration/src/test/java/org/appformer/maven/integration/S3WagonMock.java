package org.appformer.maven.integration;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.SessionListener;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.proxy.ProxyInfo;
import org.apache.maven.wagon.proxy.ProxyInfoProvider;
import org.apache.maven.wagon.repository.Repository;

import java.io.File;
import java.util.List;

public class S3WagonMock implements Wagon {
    private static boolean used = false;

    @Override
    public void get(String s, File file) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        used = true;
    }

    @Override
    public boolean getIfNewer(String s, File file, long l) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        return false;
    }

    @Override
    public void put(File file, String s) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {

    }

    @Override
    public void putDirectory(File file, String s) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {

    }

    @Override
    public boolean resourceExists(String s) throws TransferFailedException, AuthorizationException {
        return false;
    }

    @Override
    public List<String> getFileList(String s) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        return null;
    }

    @Override
    public boolean supportsDirectoryCopy() {
        return false;
    }

    @Override
    public Repository getRepository() {
        return null;
    }

    @Override
    public void connect(Repository repository) throws ConnectionException, AuthenticationException {

    }

    @Override
    public void connect(Repository repository, ProxyInfo proxyInfo) throws ConnectionException, AuthenticationException {

    }

    @Override
    public void connect(Repository repository, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {

    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo) throws ConnectionException, AuthenticationException {

    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfo proxyInfo) throws ConnectionException, AuthenticationException {

    }

    @Override
    public void connect(Repository repository, AuthenticationInfo authenticationInfo, ProxyInfoProvider proxyInfoProvider) throws ConnectionException, AuthenticationException {

    }

    @Override
    public void openConnection() throws ConnectionException, AuthenticationException {

    }

    @Override
    public void disconnect() throws ConnectionException {

    }

    @Override
    public void setTimeout(int i) {

    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public void setReadTimeout(int i) {

    }

    @Override
    public int getReadTimeout() {
        return 0;
    }

    @Override
    public void addSessionListener(SessionListener sessionListener) {

    }

    @Override
    public void removeSessionListener(SessionListener sessionListener) {

    }

    @Override
    public boolean hasSessionListener(SessionListener sessionListener) {
        return false;
    }

    @Override
    public void addTransferListener(TransferListener transferListener) {

    }

    @Override
    public void removeTransferListener(TransferListener transferListener) {

    }

    @Override
    public boolean hasTransferListener(TransferListener transferListener) {
        return false;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

    @Override
    public void setInteractive(boolean b) {

    }

    public static boolean wasUsed() {
        return used;
    }
}
