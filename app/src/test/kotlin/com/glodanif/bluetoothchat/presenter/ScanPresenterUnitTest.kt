package com.glodanif.bluetoothchat.presenter

import android.bluetooth.BluetoothDevice
import android.net.Uri
import com.glodanif.bluetoothchat.data.model.BluetoothConnector
import com.glodanif.bluetoothchat.data.model.BluetoothScanner
import com.glodanif.bluetoothchat.data.model.FileManager
import com.glodanif.bluetoothchat.ui.presenter.ScanPresenter
import com.glodanif.bluetoothchat.ui.view.ScanView
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.experimental.EmptyCoroutineContext

class ScanPresenterUnitTest {

    @RelaxedMockK
    private lateinit var scanner: BluetoothScanner
    @RelaxedMockK
    private lateinit var connector: BluetoothConnector
    @RelaxedMockK
    private lateinit var fileModel: FileManager
    @RelaxedMockK
    private lateinit var view: ScanView

    lateinit var presenter: ScanPresenter

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        presenter = ScanPresenter(view, scanner, connector, fileModel, EmptyCoroutineContext, EmptyCoroutineContext)
    }

    @Test
    fun availability_isAvailable() {
        every { scanner.isBluetoothAvailable() } returns true
        presenter.checkBluetoothAvailability()
        verify { view.showBluetoothScanner() }
    }

    @Test
    fun availability_isNotAvailable() {
        every { scanner.isBluetoothAvailable() } returns false
        presenter.checkBluetoothAvailability()
        verify { view.showBluetoothIsNotAvailableMessage() }
    }

    @Test
    fun enabling_isEnabled_isDiscoverable() {
        val paired = mockk<List<BluetoothDevice>>()
        every { scanner.getBondedDevices() } returns paired
        every { scanner.isBluetoothEnabled() } returns true
        every { scanner.isDiscoverable() } returns true
        presenter.checkBluetoothEnabling()
        verify { view.showPairedDevices(paired) }
        verify { view.showDiscoverableProcess() }
    }

    @Test
    fun enabling_isEnabled_isNotDiscoverable() {
        val paired = mockk<List<BluetoothDevice>>()
        every { scanner.getBondedDevices() } returns paired
        every { scanner.isBluetoothEnabled() } returns true
        every { scanner.isDiscoverable() } returns false
        presenter.checkBluetoothEnabling()
        verify { view.showPairedDevices(paired) }
        verify { view.showDiscoverableFinished() }
    }

    @Test
    fun enabling_isDisabled() {
        every { scanner.isBluetoothEnabled() } returns false
        presenter.checkBluetoothEnabling()
        verify { view.showBluetoothEnablingRequest() }
    }

    @Test
    fun enabling_turnOn() {
        every { scanner.isBluetoothEnabled() } returns false
        presenter.turnOnBluetooth()
        verify { view.requestBluetoothEnabling() }
    }

    @Test
    fun enabling_onEnablingFailed() {
        presenter.onBluetoothEnablingFailed()
        verify { view.showBluetoothEnablingFailed() }
    }

    @Test
    fun discovery_onMadeDiscoverable() {
        presenter.onMadeDiscoverable()
        verify { view.showDiscoverableProcess() }
    }

    @Test
    fun discovery_makeDiscoverable() {
        every { scanner.isDiscoverable() } returns false
        presenter.makeDiscoverable()
        verify { view.requestMakingDiscoverable() }
    }

    @Test
    fun scanning_cancel() {
        presenter.cancelScanning()
        verify { view.showScanningStopped() }
    }

    @Test
    fun scanning_startAlreadyStarted() {
        every { scanner.isDiscovering() } returns true
        presenter.scanForDevices()
        verify { view.showScanningStopped() }
    }


    @Test
    fun apkSharing_success() {
        val uri = mockk<Uri>()
        coEvery { fileModel.extractApkFile() } returns uri
        presenter.shareApk()
        verify { view.shareApk(uri) }
    }


    @Test
    fun scanning_start() {
        val slot = slot<BluetoothScanner.ScanningListener>()
        presenter.scanForDevices()
        verify { scanner.scanForDevices(ScanPresenter.SCAN_DURATION_SECONDS) }
        verify { scanner.setScanningListener(capture(slot)) }
        val listener = slot.captured
        listener.onDiscoveryStart(0)
        verify { view.showScanningStarted(0) }
    }

    @Test
    fun scanning_finished() {
        val slot = slot<BluetoothScanner.ScanningListener>()
        verify { scanner.setScanningListener(capture(slot)) }
        val listener = slot.captured
        listener.onDiscoveryFinish()
        verify { view.showScanningStopped() }
    }

    @Test
    fun scanning_discoverableStart() {
        val slot = slot<BluetoothScanner.ScanningListener>()
        verify { scanner.setScanningListener(capture(slot)) }
        val listener = slot.captured
        listener.onDiscoverableStart()
        verify { view.showDiscoverableProcess() }
    }

    @Test
    fun scanning_discoverableFinishStart() {
        val slot = slot<BluetoothScanner.ScanningListener>()
        verify { scanner.setScanningListener(capture(slot)) }
        val listener = slot.captured
        listener.onDiscoverableFinish()
        verify { view.showDiscoverableFinished() }
    }

    @Test
    fun scanning_onFoundDevice() {
        val slot = slot<BluetoothScanner.ScanningListener>()
        val device = mockk<BluetoothDevice>()
        verify { scanner.setScanningListener(capture(slot)) }
        val listener = slot.captured
        listener.onDeviceFind(device)
        verify { view.addFoundDevice(device) }
    }
}