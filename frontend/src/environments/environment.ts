type RuntimeWindow = Window & {
  __APP_CONFIG__?: {
    apiUrl?: string;
  };
};

const runtimeWindow = typeof window !== 'undefined' ? (window as RuntimeWindow) : undefined;
const runtimeApiUrl = runtimeWindow?.__APP_CONFIG__?.apiUrl;
const hostname = runtimeWindow?.location.hostname ?? 'localhost';
const isLocalHost = hostname === 'localhost' || hostname === '127.0.0.1' || hostname.startsWith('192.168.');

export const environment = {
  apiUrl: runtimeApiUrl || (isLocalHost ? `http://${hostname}:8080/api` : '/api'),
};
