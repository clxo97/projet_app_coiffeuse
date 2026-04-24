type RuntimeWindow = Window & {
  __APP_CONFIG__?: {
    apiUrl?: string;
  };
};

const runtimeWindow = typeof window !== 'undefined' ? (window as RuntimeWindow) : undefined;
const runtimeApiUrl = runtimeWindow?.__APP_CONFIG__?.apiUrl;
const hostname = runtimeWindow?.location.hostname ?? 'localhost';

export const environment = {
  apiUrl: runtimeApiUrl || `http://${hostname}:8080/api`,
};
