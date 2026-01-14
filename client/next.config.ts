import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: false, // para no duplicar consultas
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8081/api/:path*', // proxy for backend, chages in production
      }
    ];
  }
};

export default nextConfig;
