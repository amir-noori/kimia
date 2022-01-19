package ir.kimia.client.service.di;

import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = {ServiceModule.class})
public interface ServiceComponent {

    FxAppComponent.Builder fxApp();

}